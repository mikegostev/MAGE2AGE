package uk.ac.ebi.biosd.coriell;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Coriell implements Serializable
{
 private static final long serialVersionUID = -231200929771217407L;

 private Map<String, Sample> sampleMap = new HashMap<String, Sample>();
 private Map<String, Family> familyMap = new HashMap<String, Family>();
 private Map<String, Panel>  panelMap = new HashMap<String, Panel>();
 
 public void addSample( Sample s )
 {
  sampleMap.put(s.getId(),s);
 }
 
 public void addFamily( Family s )
 {
  familyMap.put(s.getId(),s);
 }
 
 public void addPanel( Panel s )
 {
  panelMap.put(s.getId(),s);
 }
 
 public Sample getSample( String id )
 {
  return sampleMap.get(id);
 }
 
 public Family getFamily( String id )
 {
  return familyMap.get(id);
 }

 public Panel getPanel( String id )
 {
  return panelMap.get(id);
 }

 public int size()
 {
  return panelMap.size()+familyMap.size()+panelMap.size();
 }

 public Map<String, Sample> getSamples()
 {
  return sampleMap;
 }

 public Map<String, Panel>  getPanels()
 {
  return panelMap;
 }

 public Map<String, Family>  getFamilies()
 {
  return familyMap;
 }
}
