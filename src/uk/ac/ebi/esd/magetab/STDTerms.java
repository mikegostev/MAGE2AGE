package uk.ac.ebi.esd.magetab;

import java.util.HashSet;

public class STDTerms extends HashSet<String>
{
 private static STDTerms inst = new STDTerms(); 
 
 public STDTerms()
 {
  HashSet<String> std = this;
  
  std.add("Description");
  std.add("Haplotype");
  std.add("GrowthCondition");
  std.add("Compound");
  std.add("Host");
  std.add("BioSourceType");
  std.add("Nutrients");
  std.add("BioSourceProvider");
  std.add("Test");
  std.add("CellularComponent");
  std.add("ClinicalTreatment");
  std.add("CancerSite");
  std.add("BioSource");
  std.add("Light");
  std.add("OperatorVariation");
  std.add("Organism");
  std.add("Sex");
  std.add("EnvironmentalHistory");
  std.add("Allele");
  std.add("Media");
  std.add("TestResult");
  std.add("DiseaseState");
  std.add("OrganismStatus");
  std.add("InitialTimePoint");
  std.add("Humidity");
  std.add("FamilyRelationship");
  std.add("Serotype");
  std.add("Ecotype");
  std.add("OrganismPartDatabase");
  std.add("ConcentrationUnitOther");
  std.add("PopulationDensity");
  std.add("TestType");
  std.add("DeliveryMethod");
  std.add("TimeUnit");
  std.add("Age");
  std.add("Cultivar");
  std.add("DiseaseLocation");
  std.add("CellLineDatabase");
  std.add("Ploidy");
  std.add("StrainOrLine");
  std.add("DiseaseStateDatabase");
  std.add("TumorGrading");
  std.add("ClinicalHistory");
  std.add("GeographicLocation");
  std.add("Treatment");
  std.add("Water");
  std.add("Phenotype");
  std.add("Action");
  std.add("SamplingTimePoint");
  std.add("EnvironmentalStress");
  std.add("ChromosomalAberration");
  std.add("CellLine");
  std.add("CellType");
  std.add("DevelopmentalStage");
  std.add("OrganismPart");
  std.add("IndividualGeneticCharacteristics");
  std.add("FamilyHistory");
  std.add("Temperature");
  std.add("MaterialType");
  std.add("Generation");
  std.add("Observation");
  std.add("Biometrics");
  std.add("Individual");
  std.add("DiseaseStaging");
  std.add("Genotype");
  std.add("TemperatureUnit");
  std.add("TargetedCellType");
  std.add("Histology");
  std.add("IndividualChromosomalAbnormality");
  std.add("StrainOrLineDatabase");
  std.add("ChromosomalAberrationClassification");
  std.add("GeneticModification");
  std.add("BioMaterialPurity");
 }
 
 public static boolean containsTerm(String t)
 {
  return inst.contains(t);
 }
}
