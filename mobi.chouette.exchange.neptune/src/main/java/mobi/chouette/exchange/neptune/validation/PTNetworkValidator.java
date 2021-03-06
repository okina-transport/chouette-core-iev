package mobi.chouette.exchange.neptune.validation;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mobi.chouette.common.Context;
import mobi.chouette.exchange.neptune.Constant;
import mobi.chouette.exchange.validation.ValidationData;
import mobi.chouette.exchange.validation.ValidationException;
import mobi.chouette.exchange.validation.Validator;
import mobi.chouette.exchange.validation.ValidatorFactory;
import mobi.chouette.exchange.validation.report.DataLocation;
import mobi.chouette.exchange.validation.report.ValidationReporter;
import mobi.chouette.model.ChouetteIdentifiedObject;
import mobi.chouette.model.Network;
import mobi.chouette.model.util.Referential;

public class PTNetworkValidator extends AbstractValidator implements Validator<Network> , Constant{

	public static final String LINE_ID = "lineId";
	
	public static final String SOURCE_TYPE = "sourceType";

	public static String NAME = "PTNetworkValidator";
	
	private static final String NETWORK_1 = "2-NEPTUNE-Network-1";
	private static final String NETWORK_2 = "2-NEPTUNE-Network-2";

	public static final String LOCAL_CONTEXT = "PTNetwork";

    @Override
	protected void initializeCheckPoints(Context context)
	{
		addItemToValidation(context, prefix, "Network", 2, "W", "W");
	}

	public void addLocation(Context context, ChouetteIdentifiedObject object, int lineNumber, int columnNumber)
	{
		addLocation( context,LOCAL_CONTEXT,  object,  lineNumber,  columnNumber);
		
	}
	
	@SuppressWarnings("unchecked")
	public void addLineId(Context  context, String objectId, String lineId)
	{
		Context objectContext = getObjectContext(context, LOCAL_CONTEXT, objectId);
		List<String> lineIds = (List<String>) objectContext.get(LINE_ID);
		if (lineIds == null)
		{
			lineIds = new ArrayList<>();
			objectContext.put(LINE_ID, lineIds);
		}
		lineIds.add(lineId);
	}
	
	public void addSourceType(Context  context, String objectId, String type)
	{
		Context objectContext = getObjectContext(context, LOCAL_CONTEXT, objectId);
		objectContext.put(SOURCE_TYPE, type);
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	public void validate(Context context, Network target) throws ValidationException
	{
		Context validationContext = (Context) context.get(VALIDATION_CONTEXT);
		Context localContext = (Context) validationContext.get(LOCAL_CONTEXT);
		if (localContext == null || localContext.isEmpty()) return ;
		ValidationData data = (ValidationData) context.get(VALIDATION_DATA);
		Map<String, DataLocation> fileLocations = data.getDataLocations();
		Context lineContext = (Context) validationContext.get(LineValidator.LOCAL_CONTEXT);
		Referential referential = (Referential) context.get(REFERENTIAL);
		Map<String, Network> networks = referential.getPtNetworks();

		String lineId = lineContext.keySet().iterator().next(); 

		for (String objectId : localContext.keySet()) 
		{
			// 2-NEPTUNE-PtNetwork-1 : check if lineId of line is present in list
			Context objectContext = (Context) localContext.get(objectId);
			List<String> lineIds = (List<String>) objectContext.get(LINE_ID);
			if (lineIds != null)
			{
				prepareCheckPoint(context, NETWORK_1);
				if (!lineIds.contains(lineId))
				{
					ValidationReporter validationReporter = ValidationReporter.Factory.getInstance();
					validationReporter.addCheckPointReportError(context, NETWORK_1, fileLocations.get(objectId), lineId);
				}
			}
			
			// 2-NEPTUNE-PTNetwork-2 : check if source_type is valid for neptune
			String sourceType = (String) objectContext.get(SOURCE_TYPE);
			if (sourceType != null)
			{
				prepareCheckPoint(context, NETWORK_2);
				Network network = networks.get(objectId);
				if (!sourceType.equals(network.getSourceType().name()))
				{
					ValidationReporter validationReporter = ValidationReporter.Factory.getInstance();
					validationReporter.addCheckPointReportError(context, NETWORK_2, fileLocations.get(objectId), sourceType,network.getSourceType().name());
				}
			}

		}
		return ;
	}

	public static class DefaultValidatorFactory extends ValidatorFactory {

		

		@Override
		protected Validator<Network> create(Context context) {
			PTNetworkValidator instance = (PTNetworkValidator) context.get(NAME);
			if (instance == null) {
				instance = new PTNetworkValidator();
				context.put(NAME, instance);
			}
			return instance;
		}

	}

	static {
		ValidatorFactory.factories
		.put(PTNetworkValidator.class.getName(), new DefaultValidatorFactory());
	}



}
