package mobi.chouette.exchange.importer.inserter;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import mobi.chouette.common.Context;
import mobi.chouette.dao.TimebandDAO;
import mobi.chouette.model.JourneyFrequency;
import mobi.chouette.model.Timeband;
import mobi.chouette.model.util.ObjectFactory;
import mobi.chouette.model.util.Referential;

@Stateless(name = JourneyFrequencyInserter.BEAN_NAME)
public class JourneyFrequencyInserter implements Inserter<JourneyFrequency> {

	public static final String BEAN_NAME = "JourneyFrequencyUpdater";

	@EJB 
	private TimebandDAO timebandDAO;

	@EJB(beanName = TimebandInserter.BEAN_NAME)
	private Inserter<Timeband> timebandUpdater;

	@Override
	public void insert(Context context, JourneyFrequency oldValue, JourneyFrequency newValue) throws Exception {
		Referential cache = (Referential) context.get(CACHE);

		if (newValue.getFirstDepartureTime() != null
				&& !newValue.getFirstDepartureTime().equals(oldValue.getFirstDepartureTime())) {
			oldValue.setFirstDepartureTime(newValue.getFirstDepartureTime());
		}
		if (newValue.getLastDepartureTime() != null
				&& !newValue.getLastDepartureTime().equals(oldValue.getLastDepartureTime())) {
			oldValue.setLastDepartureTime(newValue.getLastDepartureTime());
		}
		if (newValue.getScheduledHeadwayInterval() != null
				&& !newValue.getScheduledHeadwayInterval().equals(oldValue.getScheduledHeadwayInterval())) {
			oldValue.setScheduledHeadwayInterval(newValue.getScheduledHeadwayInterval());
		}
		
		// Timeband
		if (newValue.getTimeband() == null) {
			oldValue.setTimeband(null);
		}  else {
			String objectId = newValue.getTimeband().getObjectId();
			Timeband timeband = cache.getTimebands().get(objectId);
			if (timeband == null) {
				timeband = timebandDAO.findByObjectId(objectId);
				if (timeband != null) {
					cache.getTimebands().put(objectId, timeband);
				}
			}
			if (timeband == null) {
				timeband = ObjectFactory.getTimeband(cache, objectId);
			}
			oldValue.setTimeband(timeband);
			timebandUpdater.insert(context,  oldValue.getTimeband(), newValue.getTimeband());
		}
	}
}
