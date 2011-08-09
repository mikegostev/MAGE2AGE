package uk.ac.ebi.sampletab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ebi.sampletab.STParser.Group;
import uk.ac.ebi.sampletab.STParser.Sample;

import com.pri.util.collection.IntHashMap;
import com.pri.util.collection.IntMap;

public class Submission extends ContainerObject
{
 private IntMap<List<Sample>> sampleBlocks = new IntHashMap<List<Sample>>();
 private Map<String,Sample> sampleMap = new HashMap<String, STParser.Sample>();

 private IntMap<List<Group>> groupBlocks = new IntHashMap<List<Group>>();
 private Map<String,Group> groupMap = new HashMap<String, STParser.Group>();
 
 public Sample addSample(Sample sample)
 {
  Sample s = sampleMap.get(sample.getValue());
  
  if( s != null )
  {
   if( s.getBlock() != sample.getBlock() )
     throw new RuntimeException("Sample accession redefinition: "+sample.getValue());
  
   return s;
  } 
  
  sampleMap.put( sample.getValue(), sample );
  
  List<Sample> blkList = sampleBlocks.get(sample.getBlock());
  
  if( blkList == null )
   sampleBlocks.put(sample.getBlock(), blkList = new ArrayList<STParser.Sample>(100) );
  
  blkList.add(sample);
  
  return sample;
 }

 public Group addGroup(Group group)
 {
  Group g = groupMap.get(group.getValue());
  
  if( g != null )
  {
   if( g.getBlock() != group.getBlock() )
     throw new RuntimeException("Group accession redefinition: "+group.getValue());
  
   return g;
  } 
  
  groupMap.put( group.getValue(), group );
  
  List<Group> blkList = groupBlocks.get(group.getBlock());
  
  if( blkList == null )
   groupBlocks.put(group.getBlock(), blkList = new ArrayList<STParser.Group>(100) );
  
  blkList.add(group);
  
  return group;
 }

}
