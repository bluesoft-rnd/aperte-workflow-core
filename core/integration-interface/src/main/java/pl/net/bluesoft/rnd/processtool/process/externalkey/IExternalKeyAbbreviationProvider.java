package pl.net.bluesoft.rnd.processtool.process.externalkey;

/**
 * Abbreviation provider for process external key generation
 *
 * @author: mpawlak@bluesoft.net.pl
 */
public interface IExternalKeyAbbreviationProvider
{
    String getAbbreviation();

    String getFullName();
}
