package fr.certu.chouette.service.importateur.multilignes.pegase;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import chouette.schema.types.TransportModeNameType;
import fr.certu.chouette.modele.ArretItineraire;
import fr.certu.chouette.modele.Mission;
import fr.certu.chouette.modele.PositionGeographique;
import fr.certu.chouette.service.identification.IIdentificationManager;

public class Ligne {
	
	private String                         reg;
	private String                         name;
	private String                         shortName;
	private String                         codeItineraire;
	private Map<String, Itineraire>        its;
	private IIdentificationManager         identificationManager;
	private fr.certu.chouette.modele.Ligne ligne;
	private List<PositionGeographique>     zonesCommerciales;
	private Connection                     connexion;
	
	public Ligne(IIdentificationManager identificationManager, String reg, String shortName, String name, String codeItineraire) {
		this.identificationManager = identificationManager;
		this.reg = reg;
		this.shortName = shortName;
		this.name = name;
		this.its = new HashMap<String, Itineraire>();
		this.codeItineraire = codeItineraire;
    }
	
	public void setCodeItineraire(String codeItineraire) {
		this.codeItineraire = codeItineraire;
	}
	
	public String getCodeItineraire() {
		return codeItineraire;
	}
	
	public void setReg(String reg) {
		this.reg = reg;
	}
	
	public String getReg() {
		return reg;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public Map<String, Itineraire> getItineraires() {
		return its;
	}
	
	public Itineraire getItineraire(String codeIti, String shortName, String name) throws ItineraireException {
		Itineraire it = its.get(codeIti);
		if (it != null) {
			/*
			if (!it.getShortName().equals(shortName))
				throw new ItineraireException("ERROR POUR SHORTNAME : "+shortName+" : "+it.getShortName());
			if (!it.getName().equals(name))
				throw new ItineraireException("ERROR POUR NAME : "+name+" : "+it.getName());
				*/
			return it;
		}
		it = new Itineraire(identificationManager, this, codeIti, shortName, name);
		its.put(codeIti, it);
		return it;
	}
	
	public fr.certu.chouette.modele.Ligne getLigne() {
		if (ligne == null)
			ligne();
		return ligne;
	}
	
	private void ligne() {
		ligne = new fr.certu.chouette.modele.Ligne();
		ligne.setComment("Ligne de bus : "+this.getName()+".");
		ligne.setCreationTime(new Date());
		ligne.setName(codeItineraire);
		ligne.setNumber(this.getReg());
		ligne.setObjectId(identificationManager.getIdFonctionnel("Line", "NEW_"+String.valueOf(LecteurPrincipal.counter++)));
		ligne.setObjectVersion(1);
		ligne.setPublishedName(this.getShortName());
		//ligne.setRegistrationNumber(this.getReg());
		ligne.setRegistrationNumber(codeItineraire);
		ligne.setTransportModeName(TransportModeNameType.BUS);
	}
	
	private void zonesCommerciales(Connection connexion) {
		this.connexion = connexion;
		zonesCommerciales = new ArrayList<PositionGeographique>();
		Set<PositionGeographique> zcs = new HashSet<PositionGeographique>();
		for (Itineraire it : this.getItineraires().values())
			zcs.addAll(it.getZonesCommerciales(connexion));
		for (PositionGeographique zc : zcs)
			zonesCommerciales.add(zc);
	}
	
	public List<fr.certu.chouette.modele.TableauMarche> getTableauxMarche() {
		List<fr.certu.chouette.modele.TableauMarche> tableauxMarche = new ArrayList<fr.certu.chouette.modele.TableauMarche>();
		Set<fr.certu.chouette.modele.TableauMarche> tms = new HashSet<fr.certu.chouette.modele.TableauMarche>();
		for (Itineraire it : this.getItineraires().values())
			tms.addAll(it.getTableauxMarche());
		for (fr.certu.chouette.modele.TableauMarche tm : tms)
			tableauxMarche.add(tm);
		return tableauxMarche;
	}

	public List<PositionGeographique> getZonesCommerciales(Connection connexion) {
		if (zonesCommerciales == null)
			zonesCommerciales(connexion);
		return zonesCommerciales;
	}
	
	public List<PositionGeographique> getArretsPhysiques() {
		List<PositionGeographique> arretsPhysiques = new ArrayList<PositionGeographique>();
		Set<PositionGeographique> aps = new HashSet<PositionGeographique>();
		for (Itineraire it : this.getItineraires().values())
			aps.addAll(it.getArretsPhysiques());
		for (PositionGeographique arretPhysique : aps)
			arretsPhysiques.add(arretPhysique);
		return arretsPhysiques;
	}
	
	public Map<String, String> getZoneParenteParObjectId() {
		Map<String, String> zoneParenteParObjectId = new HashMap<String, String>();
		for (Itineraire it : this.getItineraires().values())
			for (Course co : it.getCourses().values())
				for (Horaire ho : co.getHoraires().values()) {
					PositionGeographique arretPhysique = ho.getArretPhysique();
					String key = arretPhysique.getObjectId();
					PositionGeographique zoneCommerciale = ho.getZoneCommerciale(connexion);
					String value = zoneCommerciale.getObjectId();
					zoneParenteParObjectId.put(key, value);
				}
		return zoneParenteParObjectId;
	}

	public List<String> getObjectIdZonesGeneriques() {
		Set<String> objectIds = new HashSet<String>();
		for (Itineraire it : this.getItineraires().values())
			for (Course co : it.getCourses().values())
				for (Horaire ho : co.getHoraires().values()) {
					objectIds.add(ho.getArretPhysique().getObjectId());
					objectIds.add(ho.getZoneCommerciale(connexion).getObjectId());
				}
		List<String> tmpObjectIds = new ArrayList<String>();
		for (String objectId : objectIds)
			tmpObjectIds.add(objectId);
		return tmpObjectIds;
	}

	public List<fr.certu.chouette.modele.Itineraire> getChouetteItineraires() {
		List<fr.certu.chouette.modele.Itineraire> chouetteItineraires = new ArrayList<fr.certu.chouette.modele.Itineraire>();
		for (Itineraire it : this.getItineraires().values())
			chouetteItineraires.add(it.getChouetteItineraire());
		return chouetteItineraires;
	}

	public List<Mission> getMissions() {
		List<Mission> missions = new ArrayList<Mission>();
		for (Itineraire it : this.getItineraires().values())
			missions.addAll(it.getMissions());
		return missions;
	}

	public List<fr.certu.chouette.modele.Course> getCourses() {
		List<fr.certu.chouette.modele.Course> vehicleJourneys = new ArrayList<fr.certu.chouette.modele.Course>();
		for (Itineraire it : this.getItineraires().values())
			vehicleJourneys.addAll(it.getVehicleJourneys());
		return vehicleJourneys;
	}

	public List<ArretItineraire> getArretsItineraires() {
		List<ArretItineraire> arretsItineraires = new ArrayList<ArretItineraire>();
		for (Itineraire it : this.getItineraires().values())
			arretsItineraires.addAll(it.getArretsItineraires());
		return arretsItineraires;
	}
	
	public Map<String, String> getItineraireParArret() {
		Map<String, String> itineraireParArret = new HashMap<String, String>();
		for (Itineraire it : this.getItineraires().values())
			for (ArretItineraire arretItineraire : it.getArretsItineraires())
				itineraireParArret.put(arretItineraire.getObjectId(), it.getChouetteItineraire().getObjectId());
		return itineraireParArret;
	}

	public List<fr.certu.chouette.modele.Horaire> getHoraires() {
		List<fr.certu.chouette.modele.Horaire> horaires = new ArrayList<fr.certu.chouette.modele.Horaire>();
		for (Itineraire it : this.getItineraires().values())
			horaires.addAll(it.getHoraires());
		return horaires;
	}
}