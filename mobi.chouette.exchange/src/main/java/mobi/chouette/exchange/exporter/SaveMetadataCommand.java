package mobi.chouette.exchange.exporter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.naming.InitialContext;

import lombok.extern.log4j.Log4j;
import mobi.chouette.common.Color;
import mobi.chouette.common.Constant;
import mobi.chouette.common.Context;
import mobi.chouette.common.JobData;
import mobi.chouette.common.chain.Command;
import mobi.chouette.common.chain.CommandFactory;
import mobi.chouette.exchange.metadata.DublinCoreFileWriter;
import mobi.chouette.exchange.metadata.Metadata;
import mobi.chouette.exchange.metadata.TextFileWriter;
import mobi.chouette.exchange.parameters.AbstractExportParameter;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

@Log4j
public class SaveMetadataCommand implements Command {

	public static final String COMMAND = "SaveMetadataCommand";

	@Override
	public boolean execute(Context context) throws Exception {

		boolean result = Constant.ERROR;

		Monitor monitor = MonitorFactory.start(COMMAND);
		JobData jobData = (JobData) context.get(Constant.JOB_DATA);

		Metadata metadata = (Metadata) context.get(Constant.METADATA);
		AbstractExportParameter parameters = (AbstractExportParameter) context.get(Constant.CONFIGURATION);
		String creator = parameters.getReferentialName();
		if (creator == null)
			creator = jobData.getReferential();
		String publisher = parameters.getOrganisationName();
		if (publisher == null)
			publisher = parameters.getName();
		try {
			if (metadata == null)
				return Constant.SUCCESS;
			// force time window if asked
			metadata.getTemporalCoverage().force(parameters.getStartDate(), parameters.getEndDate());
			metadata.setCreator(creator);
			metadata.setPublisher(publisher);
			String path = jobData.getPathName();
			Path target = Paths.get(path, Constant.OUTPUT);
			DublinCoreFileWriter dcWriter = new DublinCoreFileWriter();

			dcWriter.writePlainFile(metadata, target);
			TextFileWriter tWriter = new TextFileWriter();
			tWriter.writePlainFile(metadata, target);
			result = Constant.SUCCESS;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			log.info(Color.MAGENTA + monitor.stop() + Color.NORMAL);
		}

		return result;
	}

	public static class DefaultCommandFactory extends CommandFactory {

		@Override
		protected Command create(InitialContext context) throws IOException {
			return new SaveMetadataCommand();
		}
	}

	static {
		CommandFactory.register(SaveMetadataCommand.class.getName(), new DefaultCommandFactory());
	}
}
