package io.krisrice.utplsql;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.utplsql.api.TestRunner;
import org.utplsql.api.reporter.DocumentationReporter;
import org.utplsql.api.reporter.Reporter;

import oracle.dbtools.db.DBUtil;
import oracle.dbtools.extension.SQLCLService;
import oracle.dbtools.raptor.newscriptrunner.CommandListener;
import oracle.dbtools.raptor.newscriptrunner.IHelp;
import oracle.dbtools.raptor.newscriptrunner.ISQLCommand;
import oracle.dbtools.raptor.newscriptrunner.ScriptRunnerContext;
import oracle.dbtools.raptor.newscriptrunner.ScriptUtils;

public class UTPLsqlCommand extends CommandListener implements IHelp, SQLCLService {

	private static final String UTPLSQL_AUTO_RUN = "UTPLSQL.AUTO.RUN";

	@Override
	public Class<? extends CommandListener> getCommandListener() {
		return UTPLsqlCommand.class;
	}

	@Override
	public String getCommand() {
		// command used at the sql>
		return "utplsql";
	}

	@Override
	public String getHelp() {
		return "runs awesome tests";
	}

	@Override
	public boolean isSqlPlus() {
		// not a sqlplus feature
		return false;
	}

	@Override
	public boolean handleEvent(Connection conn, ScriptRunnerContext ctx, ISQLCommand cmd) {
		// handles the "utplsql auto on|off"
		
		if (matches(getCommand(),cmd.getSql())) {
			String[] parts = cmd.getSql().split("\\W");
			
			// stash to the context on/off
			if ( parts[1].equalsIgnoreCase("auto") && parts[2].equalsIgnoreCase("on")) {
				
				ctx.putProperty(UTPLSQL_AUTO_RUN, Boolean.TRUE);
				ctx.write("UTPLSQL Auto enabled\n");
			} else if ( parts[1].equalsIgnoreCase("auto") && parts[2].equalsIgnoreCase("off")) {
				
				ctx.putProperty(UTPLSQL_AUTO_RUN, Boolean.FALSE);
				ctx.write("UTPLSQL Auto disabled\n");
			}
			// this class handled the command
			return true;
		}
		// this class did not handle the command
		return false;
	}

	@Override
	public void beginEvent(Connection conn, ScriptRunnerContext ctx, ISQLCommand cmd) {

	}
	@Override
	public void endEvent(Connection conn, ScriptRunnerContext ctx, ISQLCommand cmd) {
		// run after the base command completed.
		// TODO  NOT run on a failed compile
		// TODO collect paths for endScript
		// TODO  Move to endScript for scripts compiling multiple objects
		//            public void endScript(Connection conn, ScriptRunnerContext ctx) {
		// TODO move test to background thread via  ctx.cloneCLIConnection();

		if (ctx.getProperty(UTPLSQL_AUTO_RUN)!= null && (Boolean)ctx.getProperty(UTPLSQL_AUTO_RUN)) {
			// only run when a create plsql object is found
			  if ( cmd.isCreatePLSQLCmd()  /* ALL Creates == cmd.isCreateCmd() */) {
				  
				  String[] obj = ScriptUtils.parseNameAndTypeUtil(cmd.getSql());
				  String owner = obj[0];
				  String name  = obj[1];
				  String type  = obj[2];
				  
				  HashMap<String, String> binds= new HashMap<String,String>();
				  
				  binds.put("obj_name", name);
				  
				  
				  DBUtil dbUtil = DBUtil.getInstance(conn);
				  
				  // check ALL_DEPENDENCIES joined to get_suites_info to see if an object is part if a suite
				  List<Map<String, ?>> ret = dbUtil.executeReturnList(
						 "select name,REFERENCED_NAME , path\n" + 
				  		" from ALL_DEPENDENCIES ad,\n" + 
				  		"     table(ut_runner.get_suites_info()) \n" + 
				  		" where REFERENCED_NAME = :obj_name "
				  		+ "and name != :obj_name \n" + 
				  		" and  item_type = 'UT_SUITE'", binds);
				  
				  
				  List<String> paths = new ArrayList<String>();
				  for(Map<String, ?> m:ret) {
					  ctx.write("SUITE Detected. Running Suite: "+ m.get("PATH") +" ...\n");
					  paths.add((String)m.get("PATH"));
				  }

				  
				  
				    try {
				        Reporter documentationReporter = new DocumentationReporter().init(conn);
				        // do it !
						new TestRunner().addPathList(paths)
						.addReporter(documentationReporter)
						.run(conn);
						
						
						   documentationReporter
					        .getOutputBuffer()
					        .setFetchSize(1)
					        .printAvailable(conn, new PrintStream(ctx.getOutputStream()));
						   
						   
					} catch (SQLException e) {
						ctx.write("UTPLSQL ERROR" + e.getMessage() + "\n");
					}				  
//				  
			  }
			}
		
	}


}
