package uk.ac.ebi.biosd.coriell;

import java.util.HashMap;
import java.util.Map;

public class CollectionMapping
{
 public static Map<String, CollectionInfo> map = new HashMap<String, CollectionMapping.CollectionInfo>()
 {{
  
  put("COHORT Project", new CollectionInfo("COHORT", "http://ccr.coriell.org/Sections/Collections/COHORT?SsId=44", "The Cooperative Huntingtons Observational Trial Repository has been established as a resource for the discovery of information related to Huntingtons disease and its causes, progression, treatments, and possible cures. This is a growing bank for DATA and SPECIMENS to accelerate research on Huntingtons disease. "));
  
  put("Wistar Collection", new CollectionInfo("WISTAR", "http://ccr.coriell.org/Sections/Collections/WISTAR?SsId=74", "The Wistar Institute collection at Coriell contains cell lines that have been developed by Wistar scientists. These materials are offered for non-commercial research conducted by universities, government agencies and academic research centers. The Wistar Institute collection currently contains a group of hybridomas that produce monoclonal antibodies that are useful in influenza research and vaccine development."));
  
  put("Integrated Primate Biomaterials Resource", new CollectionInfo("IPBIR", "http://ccr.coriell.org/Sections/Collections/IPBIR?SsId=18", "The purpose of the IPBIR - Integrated Primate Biomaterials and Information Resource is to assemble, characterize, and distribute high-quality DNA samples of known provenance with accompanying demographic, geographic, and behavioral information in order to stimulate and facilitate research in primate genetic diversity and evolution, comparative genomics, and population genetics."));

  put("NIA Aging Cell Culture Repository", new CollectionInfo("NIA", "http://ccr.coriell.org/Sections/Collections/NIA?SsId=9", "Sponsored by the National Institute on Aging (NIA), the AGING CELL REPOSITORY, is a resource facilitating cellular and molecular research studies on the mechanisms of aging and the degenerative processes associated with it. The cells in this resource have been collected over the past three decades using strict diagnostic criteria and banked under the highest quality standards of cell culture. Scientists use the highly-characterized, viable, and contaminant-free cell cultures from this collection for research on such diseases as Alzheimer disease, progeria, Parkinsonism, Werner syndrome, and Cockayne syndrome."));

  put("Yerkes Primates", new CollectionInfo("YERKES", "http://ccr.coriell.org/Sections/Collections/YERKES?SsId=66", "The Yerkes National Primate Research Center of Emory University is an international leader in biomedical and behavioral research. For more than seven decades, the Yerkes Research Center has been dedicated to advancing scientific understanding of primate biology, behavior, veterinary care and conservation, and to improving human health and well-being."));
 
  put("NIGMS Human Genetic Cell Repository", new CollectionInfo("NIGMS", "http://ccr.coriell.org/Sections/Collections/NIGMS?SsId=8", "The Human Genetic Cell Repository, sponsored by the National Institute of General Medical Sciences, provides scientists around the world with resources for cell and genetic research. The samples include highly characterized cell lines and high quality DNA. Repository samples represent a variety of disease states, chromosomal abnormalities, apparently healthy individuals and many distinct human populations."));

  put("Leiomyosarcoma", new CollectionInfo("LMS", "http://ccr.coriell.org/Sections/Collections/LMS?SsId=17", "The Leiomyosarcoma Cell and DNA Repository has been established with an award from the National Leiomyosarcoma Foundation. This foundation provides leadership in supporting research of Leiomyosarcoma, improving treatment outcomes of those affected by this disease as well as fostering awareness in the medical community and general public. The resources available include highly-characterized, viable, and contaminant-free cell cultures and high quality, well-characterized DNA samples derived from these cultures, both subjected to rigorous quality control."));

  put("NHGRI Sample Repository for Human Genetic Research", new CollectionInfo("NHGRI", "http://ccr.coriell.org/Sections/Collections/NHGRI?SsId=11", "The NHGRI Sample Repository for Human Genetic Research offers DNA samples and cell lines from fifteen populations, including the samples used for the International HapMap Project, the HapMap 3 Project and the 1000 Genomes Project (except for the CEPH samples). All of the samples were contributed with consent to broad data release and to their use in many future studies, including for extensive genotyping and sequencing, gene expression and proteomics studies, and all other types of genetic variation research. The samples include no identifying or phenotypic information, and are high-quality resources for the study of genetic variation in a range of human populations.") );

  put("NIAID - USIDnet", new CollectionInfo("USIDNET", "http://ccr.coriell.org/Sections/Collections/USIDNET?SsId=15", "The USIDNET DNA and Cell Repository has been established as part of an NIH-funded program - the US Immunodeficiency Network (www.usidnet.org) - to provide a resource of DNA and functional lymphoid cells obtained from patients with various primary immunodeficiency diseases. These uncommon disorders include patients with defects in T cell, B cell and/or granulocyte function as well as patients with abnormalities in antibodies/immunoglobulins, complement and other host defense mechanisms."));

  put("ADA Repository", new CollectionInfo("ADA", "http://ccr.coriell.org/Sections/Collections/ADA/?SsId=12", "The purpose of the American Diabetes Association (ADA), GENNID Study (Genetics of non-insulin dependent diabetes mellitus, NIDDM) is to establish a national database and cell repository consisting of information and genetic material from families with well-documented NIDDM. The GENNID Study will provide investigators with the information and samples necessary to conduct genetic linkage studies and locate the genes for NIDDM."));

  put("NINDS Repository", new CollectionInfo("NINDS", "http://ccr.coriell.org/Sections/Collections/NINDS/?SsId=10", "The National Institute of Neurological Disorders and Stroke is committed to gene discovery, as a strategy for identifying the genetic causes and correlates of nervous system disorders. The NINDS Human Genetics DNA and Cell Line Repository banks samples from subjects with cerebrovascular disease, epilepsy, motor neuron disease, Parkinsonism and Tourette Syndrome, as well as controls."));
  
  put("Autism", new CollectionInfo("AUTISM", "http://ccr.coriell.org/Sections/Collections/AUTISM/?SsId=13", "The State of New Jersey funded the initiation of a genetic resource to support the study of autism in families where more than one child is affected or where one child is affected and one demonstrates another significant and related developmental disorder. This resource now receives continuing support from the Coriell Institute for Medical Research. An open bank of anonymously collected materials documented by a detailed clinical diagnosis forms the basis of this growing database of information about the disease."));
 }};
 
 
 public static class CollectionInfo
 {
  String id;
  String link;
  String description;
  
  public CollectionInfo(String id, String l, String description)
  {
   super();
   this.id = id;
   this.description = description;
   link=l;
  }

  public String getId()
  {
   return id;
  }

  public void setId(String id)
  {
   this.id = id;
  }

  public String getDescription()
  {
   return description;
  }

  public void setDescription(String description)
  {
   this.description = description;
  }

  public String getLink()
  {
   return link;
  }

  public void setLink(String link)
  {
   this.link = link;
  }
  
  
 }
}
