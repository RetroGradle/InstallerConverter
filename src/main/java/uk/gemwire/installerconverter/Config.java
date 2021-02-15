package uk.gemwire.installerconverter;

import uk.gemwire.installerconverter.resolver.IResolver;

public abstract class Config {

    public static IResolver RESOLVER = (host, artifact) -> null;

}
