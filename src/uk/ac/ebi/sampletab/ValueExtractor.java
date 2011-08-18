package uk.ac.ebi.sampletab;

public interface ValueExtractor
{
 void setSample( Sample obj );
 
 String extract();
 
 boolean hasValue();
}
