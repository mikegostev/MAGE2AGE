package uk.ac.ebi.biosd.coriell;

import java.util.List;

public interface ObjGroup
{

 public abstract void addSample(String s);

 public abstract List<String> getSamples();

}