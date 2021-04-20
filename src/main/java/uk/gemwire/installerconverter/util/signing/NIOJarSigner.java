package uk.gemwire.installerconverter.util.signing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.sun.jarsigner.ContentSigner;
import com.sun.jarsigner.ContentSignerParameters;
import jdk.security.jarsigner.JarSigner;
import jdk.security.jarsigner.JarSignerException;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.pkcs.PKCS9Attributes;
import sun.security.timestamp.HttpTimestamper;
import sun.security.tools.PathList;
import sun.security.util.ManifestDigester;
import sun.security.util.SignatureFileVerifier;
import sun.security.util.SignatureUtil;
import sun.security.x509.AlgorithmId;

public class NIOJarSigner {

    public static class Builder {

        // Signer materials:
        final PrivateKey privateKey;
        final X509Certificate[] certChain;

        // JarSigner options:
        String digestalg;
        String sigalg;
        Provider digestProvider;
        Provider sigProvider;
        URI tsaUrl;
        String signerName;
        BiConsumer<String, Path> handler;

        // Implementation-specific properties:
        String tSAPolicyID;
        String tSADigestAlg;
        boolean sectionsonly = false;
        boolean internalsf = false;
        String altSignerPath;
        String altSigner;

        public Builder(KeyStore.PrivateKeyEntry entry) {
            this.privateKey = entry.getPrivateKey();
            try {
                // called internally, no need to clone
                Certificate[] certs = entry.getCertificateChain();
                this.certChain = Arrays.copyOf(certs, certs.length,
                    X509Certificate[].class);
            } catch (ArrayStoreException ase) {
                // Wrong type, not X509Certificate. Won't document.
                throw new IllegalArgumentException("Entry does not contain X509Certificate");
            }
        }

        public Builder(PrivateKey privateKey, CertPath certPath) {
            List<? extends Certificate> certs = certPath.getCertificates();
            if (certs.isEmpty()) {
                throw new IllegalArgumentException("certPath cannot be empty");
            }
            if (!privateKey.getAlgorithm().equals
                (certs.get(0).getPublicKey().getAlgorithm())) {
                throw new IllegalArgumentException("private key algorithm does not match algorithm of public key in end entity " +
                        "certificate (the 1st in certPath)");
            }
            this.privateKey = privateKey;
            try {
                this.certChain = certs.toArray(new X509Certificate[0]);
            } catch (ArrayStoreException ase) {
                // Wrong type, not X509Certificate.
                throw new IllegalArgumentException("Entry does not contain X509Certificate");
            }
        }

        public NIOJarSigner.Builder digestAlgorithm(String algorithm) throws NoSuchAlgorithmException {
            MessageDigest.getInstance(Objects.requireNonNull(algorithm));
            this.digestalg = algorithm;
            this.digestProvider = null;
            return this;
        }

        public NIOJarSigner.Builder digestAlgorithm(String algorithm, Provider provider)
            throws NoSuchAlgorithmException {
            MessageDigest.getInstance(Objects.requireNonNull(algorithm), Objects.requireNonNull(provider));
            this.digestalg = algorithm;
            this.digestProvider = provider;
            return this;
        }

        public NIOJarSigner.Builder signatureAlgorithm(String algorithm)
            throws NoSuchAlgorithmException {
            // Check availability
            Signature.getInstance(Objects.requireNonNull(algorithm));
            SignatureUtil.checkKeyAndSigAlgMatch(privateKey, algorithm);
            this.sigalg = algorithm;
            this.sigProvider = null;
            return this;
        }

        public NIOJarSigner.Builder signatureAlgorithm(String algorithm, Provider provider)
            throws NoSuchAlgorithmException {
            // Check availability
            Signature.getInstance(
                Objects.requireNonNull(algorithm),
                Objects.requireNonNull(provider));
            SignatureUtil.checkKeyAndSigAlgMatch(privateKey, algorithm);
            this.sigalg = algorithm;
            this.sigProvider = provider;
            return this;
        }

        public NIOJarSigner.Builder tsa(URI uri) {
            this.tsaUrl = Objects.requireNonNull(uri);
            return this;
        }

        public NIOJarSigner.Builder signerName(String name) {
            if (name.isEmpty() || name.length() > 8) {
                throw new IllegalArgumentException("Name too long");
            }

            name = name.toUpperCase(Locale.ENGLISH);

            for (int j = 0; j < name.length(); j++) {
                char c = name.charAt(j);
                if (!
                    ((c >= 'A' && c <= 'Z') ||
                        (c >= '0' && c <= '9') ||
                        (c == '-') ||
                        (c == '_'))) {
                    throw new IllegalArgumentException("Invalid characters in name");
                }
            }
            this.signerName = name;
            return this;
        }

        public NIOJarSigner.Builder eventHandler(BiConsumer<String, Path> handler) {
            this.handler = Objects.requireNonNull(handler);
            return this;
        }

        public NIOJarSigner.Builder setProperty(String key, String value) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
            switch (key.toLowerCase(Locale.US)) {
                case "tsadigestalg":
                    try {
                        MessageDigest.getInstance(value);
                    } catch (NoSuchAlgorithmException nsae) {
                        throw new IllegalArgumentException("Invalid tsadigestalg", nsae);
                    }
                    this.tSADigestAlg = value;
                    break;
                case "tsapolicyid":
                    this.tSAPolicyID = value;
                    break;
                case "internalsf":
                    this.internalsf = parseBoolean("interalsf", value);
                    break;
                case "sectionsonly":
                    this.sectionsonly = parseBoolean("sectionsonly", value);
                    break;
                case "altsignerpath":
                    altSignerPath = value;
                    break;
                case "altsigner":
                    altSigner = value;
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported key " + key);
            }
            return this;
        }

        private static boolean parseBoolean(String name, String value) {
            switch (value) {
                case "true":
                    return true;
                case "false":
                    return false;
                default:
                    throw new IllegalArgumentException(
                            "Invalid " + name + " value");
            }
        }

        public NIOJarSigner build() {
            return new NIOJarSigner(this);
        }
    }

    private static final String META_INF = "META-INF";

    // All fields in Builder are duplicated here as final. Those not
    // provided but has a default value will be filled with default value.

    // Precisely, a final array field can still be modified if only
    // reference is copied, no clone is done because we are concerned about
    // casual change instead of malicious attack.

    // Signer materials:
    private final PrivateKey privateKey;
    private final X509Certificate[] certChain;

    // JarSigner options:
    private final String digestalg;
    private final String sigalg;
    private final Provider digestProvider;
    private final Provider sigProvider;
    private final URI tsaUrl;
    private final String signerName;
    private final BiConsumer<String, Path> handler;

    // Implementation-specific properties:
    private final String tSAPolicyID;
    private final String tSADigestAlg;
    private final boolean sectionsonly;
    private final boolean internalsf;
    private final String altSignerPath;
    private final String altSigner;

    private NIOJarSigner(NIOJarSigner.Builder builder) {

        this.privateKey = builder.privateKey;
        this.certChain = builder.certChain;
        this.digestalg = builder.digestalg != null ? builder.digestalg : JarSigner.Builder.getDefaultDigestAlgorithm();
        this.digestProvider = builder.digestProvider;
        if (builder.sigalg != null) {
            this.sigalg = builder.sigalg;
        } else {
            this.sigalg = JarSigner.Builder
                .getDefaultSignatureAlgorithm(privateKey);
            if (this.sigalg == null) {
                throw new IllegalArgumentException("No signature alg for " + privateKey.getAlgorithm());
            }
        }
        this.sigProvider = builder.sigProvider;
        this.tsaUrl = builder.tsaUrl;

        this.signerName = builder.signerName != null ? builder.signerName : "SIGNER";
        this.handler = builder.handler;

        this.tSADigestAlg = builder.tSADigestAlg != null ? builder.tSADigestAlg : JarSigner.Builder.getDefaultDigestAlgorithm();
        this.tSAPolicyID = builder.tSAPolicyID;
        this.sectionsonly = builder.sectionsonly;
        this.internalsf = builder.internalsf;
        this.altSigner = builder.altSigner;
        this.altSignerPath = builder.altSignerPath;

        // altSigner cannot support modern algorithms like RSASSA-PSS and EdDSA
        if (altSigner != null
                && !sigalg.toUpperCase(Locale.ENGLISH).contains("WITH")) {
            throw new IllegalArgumentException(
                    "Customized ContentSigner is not supported for " + sigalg);
        }
    }

    public void sign(Path zipFile, OutputStream os) {
        try {
            sign0(Objects.requireNonNull(zipFile), Objects.requireNonNull(os));
        } catch (SocketTimeoutException | CertificateException e) {
            // CertificateException is thrown when the received cert from TSA
            // has no id-kp-timeStamping in its Extended Key Usages extension.
            throw new JarSignerException("Error applying timestamp", e);
        } catch (IOException ioe) {
            throw new JarSignerException("I/O error", ioe);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new JarSignerException("Error in signer materials", e);
        } catch (SignatureException se) {
            throw new JarSignerException("Error creating signature", se);
        }
    }

    public String getDigestAlgorithm() {
        return digestalg;
    }

    public String getSignatureAlgorithm() {
        return sigalg;
    }

    public URI getTsa() {
        return tsaUrl;
    }

    public String getSignerName() {
        return signerName;
    }

    public String getProperty(String key) {
        Objects.requireNonNull(key);
        return switch (key.toLowerCase(Locale.US)) {
            case "tsadigestalg" -> tSADigestAlg;
            case "tsapolicyid" -> tSAPolicyID;
            case "internalsf" -> Boolean.toString(internalsf);
            case "sectionsonly" -> Boolean.toString(sectionsonly);
            case "altsignerpath" -> altSignerPath;
            case "altsigner" -> altSigner;
            default -> throw new UnsupportedOperationException("Unsupported key " + key);
        };
    }

    // ****************************** PAST THIS POINT BE DRAGONS! ******************************


    private void sign0(Path zipFile, OutputStream os) throws IOException, CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        MessageDigest digest;
        try {
            if (digestProvider == null) {
                digest = MessageDigest.getInstance(digestalg);
            } else {
                digest = MessageDigest.getInstance(digestalg, digestProvider);
            }
        } catch (NoSuchAlgorithmException asae) {
            // Should not happen. User provided alg were checked, and default
            // alg should always be available.
            throw new AssertionError(asae);
        }

        try (ZipOutputStream zos = new ZipOutputStream(os); FileSystem fs = FileSystems.newFileSystem(zipFile)) {
            Manifest manifest = new Manifest();
            byte[] mfRawBytes = null;

            // Check if manifest exists
            Path mfFile = getManifestFile(fs);
            boolean mfCreated = mfFile == null;
            if (!mfCreated) {
                // Manifest exists. Read its raw bytes.
                mfRawBytes = Files.readAllBytes(mfFile);
                manifest.read(new ByteArrayInputStream(mfRawBytes));
            } else {
                // Create new manifest
                Attributes mattr = manifest.getMainAttributes();
                mattr.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
                String javaVendor = System.getProperty("java.vendor");
                String jdkVersion = System.getProperty("java.version");
                mattr.putValue("Created-By", jdkVersion + " (" + javaVendor + ")");
                mfFile = Path.of(JarFile.MANIFEST_NAME);
            }

            /*
             * For each entry in jar
             * (except for signature-related META-INF entries),
             * do the following:
             *
             * - if entry is not contained in manifest, add it to manifest;
             * - if entry is contained in manifest, calculate its hash and
             *   compare it with the one in the manifest; if they are
             *   different, replace the hash in the manifest with the newly
             *   generated one. (This may invalidate existing signatures!)
             */
            List<Path> mfFiles = new ArrayList<>();

            AtomicBoolean wasSigned = new AtomicBoolean(false);

            for (Path root : fs.getRootDirectories()) {
                for (Path path : Files.walk(root).toList()) {

                    if (path.getFileName() == null) continue;

                    if (toStr(path).startsWith(META_INF) && !Files.isDirectory(path)) {
                        // Store META-INF files in vector, so they can be written
                        // out first
                        mfFiles.add(path);

                        String zeNameUp = toStr(path).toUpperCase(Locale.ENGLISH);
                        if (SignatureFileVerifier.isBlockOrSF(zeNameUp)
                            // no need to preserve binary manifest portions
                            // if the only existing signature will be replaced
                            && !zeNameUp.startsWith(SignatureFile.getBaseSignatureFilesName(signerName))) {
                            wasSigned.set(true);
                        }

                        if (SignatureFileVerifier.isSigningRelated(toStr(path))) {
                            // ignore signature-related and manifest files
                            continue;
                        }
                    }

                    if (manifest.getAttributes(toStr(path)) != null) {
                        // jar entry is contained in manifest, check and
                        // possibly update its digest attributes
                        updateDigests(path, digest, manifest);
                    } else if (!Files.isDirectory(path)) {
                        // Add entry to manifest
                        Attributes attrs = getDigestAttributes(path, digest);

                        manifest.getEntries().put(toStr(path), attrs);
                    }
                }
            }

            /*
             * Note:
             *
             * The Attributes object is based on HashMap and can handle
             * continuation lines. Therefore, even if the contents are not changed
             * (in a Map view), the bytes that it write() may be different from
             * the original bytes that it read() from. Since the signature is
             * based on raw bytes, we must retain the exact bytes.
             */
            boolean mfModified;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (mfCreated || !wasSigned.get()) {
                mfModified = true;
                manifest.write(baos);
                mfRawBytes = baos.toByteArray();
            } else {

                // the manifest before updating
                Manifest oldManifest = new Manifest(
                    new ByteArrayInputStream(mfRawBytes));
                mfModified = !oldManifest.equals(manifest);
                if (mfModified) {
                    // reproduce the manifest raw bytes for unmodified sections
                    manifest.write(baos);
                    byte[] mfNewRawBytes = baos.toByteArray();
                    baos.reset();

                    ManifestDigester oldMd = new ManifestDigester(mfRawBytes);
                    ManifestDigester newMd = new ManifestDigester(mfNewRawBytes);

                    // main attributes
                    if (manifest.getMainAttributes().equals(oldManifest.getMainAttributes())
                        && (manifest.getEntries().isEmpty() || oldMd.getMainAttsEntry().isProperlyDelimited())) {
                        oldMd.getMainAttsEntry().reproduceRaw(baos);
                    } else {
                        newMd.getMainAttsEntry().reproduceRaw(baos);
                    }

                    // individual sections
                    for (Map.Entry<String, Attributes> entry :
                        manifest.getEntries().entrySet()) {
                        String sectionName = entry.getKey();
                        Attributes entryAtts = entry.getValue();
                        if (entryAtts.equals(oldManifest.getAttributes(sectionName))
                            && oldMd.get(sectionName).isProperlyDelimited()) {
                            oldMd.get(sectionName).reproduceRaw(baos);
                        } else {
                            newMd.get(sectionName).reproduceRaw(baos);
                        }
                    }

                    mfRawBytes = baos.toByteArray();
                }
                // if !mfModified, then leave whole manifest (mfRawBytes) unmodified
            }

            // Write out the manifest
            if (mfModified) {
                // manifest file has new length
                Files.deleteIfExists(mfFile);
            }
            if (handler != null) {
                if (mfCreated || !mfModified) {
                    handler.accept("adding", mfFile);
                } else {
                    handler.accept("updating", mfFile);
                }
            }
            Files.write(mfFile, mfRawBytes);

            // Calculate SignatureFile (".SF") and SignatureBlockFile
            ManifestDigester manDig = new ManifestDigester(mfRawBytes);
            SignatureFile sf = new SignatureFile(digest, manifest, manDig, signerName, sectionsonly);

            byte[] block;

            baos.reset();
            sf.write(baos);
            byte[] content = baos.toByteArray();

            if (altSigner == null) {
                Function<byte[], PKCS9Attributes> timestamper = null;
                if (tsaUrl != null) {
                    timestamper = s -> {
                        try {
                            // Timestamp the signature
                            HttpTimestamper tsa = new HttpTimestamper(tsaUrl);
                            byte[] tsToken = PKCS7.generateTimestampToken(
                                    tsa, tSAPolicyID, tSADigestAlg, s);

                            return new PKCS9Attributes(new PKCS9Attribute[]{
                                    new PKCS9Attribute(
                                            PKCS9Attribute.SIGNATURE_TIMESTAMP_TOKEN_OID,
                                            tsToken)});
                        } catch (IOException | CertificateException e) {
                            throw new RuntimeException(e);
                        }
                    };
                }
                // We now create authAttrs in block data, so "direct == false".
                block = PKCS7.generateNewSignedData(sigalg, sigProvider, privateKey, certChain,
                        content, internalsf, false, timestamper);
            } else {
                Signature signer = SignatureUtil.fromKey(sigalg, privateKey, sigProvider);
                signer.update(content);
                byte[] signature = signer.sign();

                @SuppressWarnings("removal")
                ContentSignerParameters params =
                        new NIOJarSignerParameters(null, tsaUrl, tSAPolicyID,
                                tSADigestAlg, signature, signer.getAlgorithm(), certChain, content);
                @SuppressWarnings("removal")
                ContentSigner signingMechanism = loadSigningMechanism(altSigner, altSignerPath);
                //noinspection removal
                block = signingMechanism.generateSignedData(
                        params,
                        !internalsf,
                        params.getTimestampingAuthority() != null
                                || params.getTimestampingAuthorityCertificate() != null);
            }

            String sfFilename = sf.getMetaName();
            String bkFilename = sf.getBlockName(privateKey);

            Path sfFile = Path.of(sfFilename);
            Path bkFile = Path.of(bkFilename);

            FileTime time = FileTime.fromMillis(System.currentTimeMillis());

            // signature file
            if (handler != null) {
                if (Files.exists(sfFile)) {
                    handler.accept("updating", sfFile);
                } else {
                    handler.accept("adding", sfFile);
                }
            }

            ZipEntry sfZipE = new ZipEntry(sfFilename);
            zos.putNextEntry(sfZipE);
            sf.write(zos);

            // signature block file
            if (handler != null) {
                if (Files.exists(bkFile)) {
                    handler.accept("updating", bkFile);
                } else {
                    handler.accept("adding", bkFile);
                }
            }

            ZipEntry bkZipE = new ZipEntry(bkFilename);
            zos.putNextEntry(bkZipE);
            zos.write(block);


            // Write out all other META-INF files that we stored in the
            // vector
            for (Path otherMFFile : mfFiles) {
                if (!toStr(otherMFFile).equalsIgnoreCase(JarFile.MANIFEST_NAME)
                    && !otherMFFile.equals(sfFile)
                    && !otherMFFile.equals(bkFile)) {
                    if (otherMFFile.startsWith(SignatureFile.getBaseSignatureFilesName(signerName))
                        && SignatureFileVerifier.isBlockOrSF(toStr(otherMFFile))) {
                        if (handler != null) {
                            handler.accept("updating", otherMFFile);
                        }
                        continue;
                    }
                    if (handler != null) {
                        if (manifest.getAttributes(toStr(otherMFFile)) != null) {
                            handler.accept("signing", otherMFFile);
                        } else if (!Files.isDirectory(otherMFFile)) {
                            handler.accept("adding", otherMFFile);
                        }
                    }
                    writeEntry(otherMFFile, zos);
                }
            }

            // Write out all other files
            for (Path root : fs.getRootDirectories()) {
                List<Path> paths = Files.walk(root).filter(Files::isRegularFile).toList();
                for (Path path : paths) {
                    if (!path.startsWith(META_INF)) {
                        if (handler != null) {
                            if (manifest.getAttributes(toStr(path)) != null) {
                                handler.accept("signing", path);
                            } else {
                                handler.accept("adding", path);
                            }
                        }
                        writeEntry(path, zos);
                    }
                }
            }
        }
    }

    private String toStr(Path path) {
        return path.toString().replaceFirst("^[\\\\/]", "");
    }

    private void writeEntry(Path input, ZipOutputStream os) throws IOException {
        ZipEntry ze2 = new ZipEntry(toStr(input));
        if (input.getFileSystem().supportedFileAttributeViews().contains("zip")) {
            if (Files.getAttribute(input, "method") instanceof Integer method) {
                ze2.setMethod(method);
            }
            ze2.setTime(Files.getLastModifiedTime(input).toMillis());
            ze2.setComment((String) Files.getAttribute(input, "comment"));
            if (ze2.getMethod() == ZipEntry.STORED) {
                ze2.setSize((Long) Files.getAttribute(input, "compressedSize"));
                ze2.setCrc((Long) Files.getAttribute(input, "crc"));
            }
        }
        os.putNextEntry(ze2);
        Files.copy(input, os);
    }

    private void updateDigests(Path path, MessageDigest digests, Manifest mf) throws IOException {
        Attributes attrs = mf.getAttributes(toStr(path));
        String base64Digests = getDigest(path, digests);

        // The entry name to be written into attrs
        String name = null;
        try {
            // Find if the digest already exists. An algorithm could have
            // different names. For example, last time it was SHA, and this
            // time it's SHA-1.
            AlgorithmId aid = AlgorithmId.get(digests.getAlgorithm());
            for (Object key : attrs.keySet()) {
                if (key instanceof Attributes.Name) {
                    String n = key.toString();
                    if (n.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST")) {
                        String tmp = n.substring(0, n.length() - 7);
                        if (AlgorithmId.get(tmp).equals(aid)) {
                            name = n;
                            break;
                        }
                    }
                }
            }
        } catch (NoSuchAlgorithmException nsae) {
            // Ignored. Writing new digest entry.
        }

        if (name == null) {
            name = digests.getAlgorithm() + "-Digest";
        }
        attrs.putValue(name, base64Digests);
    }

    private Attributes getDigestAttributes(Path path, MessageDigest digest) throws IOException {
        String base64Digests = getDigest(path, digest);
        Attributes attrs = new Attributes();
        attrs.putValue(digest.getAlgorithm() + "-Digest", base64Digests);
        return attrs;
    }

    private Path getManifestFile(FileSystem zip) throws IOException {
        Path manifest = zip.getPath(JarFile.MANIFEST_NAME);
        if (Files.exists(manifest)) {
            return manifest;
        }
        for (Path root : zip.getRootDirectories()) {
            manifest = Files.walk(root)
                .filter(path -> path.toAbsolutePath().endsWith(JarFile.MANIFEST_NAME))
                .findFirst().orElse(null);
            if (manifest != null) {
                return manifest;
            }
        }
        return manifest;
    }

    private String getDigest(Path path, MessageDigest digest) throws IOException {
        int n;
        try (InputStream is = Files.newInputStream(path)) {
            long left = Files.size(path);
            byte[] buffer = new byte[8192];
            while ((left > 0)
                && (n = is.read(buffer, 0, buffer.length)) != -1) {
                digest.update(buffer, 0, n);
                left -= n;
            }
        }

        return Base64.getEncoder().encodeToString(digest.digest());
    }

    // More dragons!

    /*
     * Try to load the specified signing mechanism.
     * The URL class loader is used.
     */
    @SuppressWarnings("removal")
    private ContentSigner loadSigningMechanism(String signerClassName,
                                               String signerClassPath) {

        // If there is no signerClassPath provided, search from here
        if (signerClassPath == null) {
            signerClassPath = ".";
        }

        // construct class loader
        String cpString;   // make sure env.class.path defaults to dot

        // do prepends to get correct ordering
        cpString = PathList.appendPath(System.getProperty("env.class.path"), null);
        cpString = PathList.appendPath(System.getProperty("java.class.path"), cpString);
        cpString = PathList.appendPath(signerClassPath, cpString);
        URL[] urls = PathList.pathToURLs(cpString);
        ClassLoader appClassLoader = new URLClassLoader(urls);

        try {
            // attempt to find signer
            Class<?> signerClass = appClassLoader.loadClass(signerClassName);
            Object signer = signerClass.getDeclaredConstructor().newInstance();
            return (ContentSigner) signer;
        } catch (ClassNotFoundException | InstantiationException |
            IllegalAccessException | ClassCastException |
            NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalArgumentException("Invalid altSigner or altSignerPath", e);
        }
    }

    static class SignatureFile {

        Manifest sf;
        String baseName;

        public SignatureFile(MessageDigest digest,
                             Manifest mf,
                             ManifestDigester md,
                             String baseName,
                             boolean sectionsonly) {

            this.baseName = baseName;

            String version = System.getProperty("java.version");
            String javaVendor = System.getProperty("java.vendor");

            sf = new Manifest();
            Attributes mattr = sf.getMainAttributes();

            mattr.putValue(Attributes.Name.SIGNATURE_VERSION.toString(), "1.0");
            mattr.putValue("Created-By", version + " (" + javaVendor + ")");

            if (!sectionsonly) {
                mattr.putValue(digest.getAlgorithm() + "-Digest-Manifest",
                    Base64.getEncoder().encodeToString(md.manifestDigest(digest)));
            }

            // create digest of the manifest main attributes
            ManifestDigester.Entry mde = md.getMainAttsEntry(false);
            if (mde != null) {
                mattr.putValue(digest.getAlgorithm() + "-Digest-" + ManifestDigester.MF_MAIN_ATTRS,
                    Base64.getEncoder().encodeToString(mde.digest(digest)));
            } else {
                throw new IllegalStateException("ManifestDigester failed to create Manifest-Main-Attribute entry");
            }

            // go through the manifest entries and create the digests
            Map<String, Attributes> entries = sf.getEntries();
            for (String name : mf.getEntries().keySet()) {
                mde = md.get(name, false);
                if (mde != null) {
                    Attributes attr = new Attributes();
                    attr.putValue(digest.getAlgorithm() + "-Digest",
                        Base64.getEncoder().encodeToString(
                            mde.digest(digest)));
                    entries.put(name, attr);
                }
            }
        }

        // Write .SF file
        public void write(OutputStream out) throws IOException {
            sf.write(out);
        }

        private static String getBaseSignatureFilesName(String baseName) {
            return "META-INF/" + baseName + ".";
        }

        // get .SF file name
        public String getMetaName() {
            return getBaseSignatureFilesName(baseName) + "SF";
        }

        // get .DSA (or .DSA, .EC) file name
        public String getBlockName(PrivateKey privateKey) {
            String keyAlgorithm = privateKey.getAlgorithm();
            return getBaseSignatureFilesName(baseName) + keyAlgorithm;
        }
    }

    @SuppressWarnings("removal")
    static class NIOJarSignerParameters implements ContentSignerParameters {

        private final String[] args;
        private final URI tsa;
        private final byte[] signature;
        private final String signatureAlgorithm;
        private final X509Certificate[] signerCertificateChain;
        private final byte[] content;
        private final String tSAPolicyID;
        private final String tSADigestAlg;

        NIOJarSignerParameters(String[] args, URI tsa,
                               String tSAPolicyID, String tSADigestAlg,
                               byte[] signature, String signatureAlgorithm,
                               X509Certificate[] signerCertificateChain,
                               byte[] content) {

            Objects.requireNonNull(signature);
            Objects.requireNonNull(signatureAlgorithm);
            Objects.requireNonNull(signerCertificateChain);

            this.args = args;
            this.tsa = tsa;
            this.tSAPolicyID = tSAPolicyID;
            this.tSADigestAlg = tSADigestAlg;
            this.signature = signature;
            this.signatureAlgorithm = signatureAlgorithm;
            this.signerCertificateChain = signerCertificateChain;
            this.content = content;
        }

        public String[] getCommandLine() {
            return args;
        }

        public URI getTimestampingAuthority() {
            return tsa;
        }

        public X509Certificate getTimestampingAuthorityCertificate() {
            // We don't use this param. Always provide tsaURI.
            return null;
        }

        public String getTSAPolicyID() {
            return tSAPolicyID;
        }

        public String getTSADigestAlg() {
            return tSADigestAlg;
        }

        public byte[] getSignature() {
            return signature;
        }

        public String getSignatureAlgorithm() {
            return signatureAlgorithm;
        }

        public X509Certificate[] getSignerCertificateChain() {
            return signerCertificateChain;
        }

        public byte[] getContent() {
            return content;
        }

        public ZipFile getSource() {
            return null;
        }
    }
}
