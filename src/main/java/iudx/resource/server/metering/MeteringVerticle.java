package iudx.resource.server.metering;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;

public class MeteringVerticle extends AbstractVerticle {

	private static final String METERING_SERVICE_ADDRESS = "iudx.rs.metering.service";
	private static final Logger LOGGER = LogManager.getLogger(MeteringVerticle.class);
	private String databaseIP;
	private int databasePort;
	private String databaseName;
	private String databaseUserName;
	private String databasePassword;
	private int poolSize;
	private PgConnectOptions config;

	private ServiceBinder binder;
	private MessageConsumer<JsonObject> consumer;
	private MeteringService metering;
	PgConnectOptions connectOptions;
	PoolOptions poolOptions;
	PgPool pool;

	
	
	@Override
	public void start() throws Exception {

		databaseIP = config().getString("meteringDatabaseIP");
		databasePort = config().getInteger("meteringDatabasePort");
		databaseName = config().getString("meteringDatabaseName");
		databaseUserName = config().getString("meteringDatabaseUserName");
		databasePassword = config().getString("meteringDatabasePassword");
		poolSize = config().getInteger("meteringpoolSize");

		JsonObject propObj = new JsonObject();
		propObj.put("meteringDatabaseIP", databaseIP);
		propObj.put("meteringDatabasePort", databasePort);
		propObj.put("meteringDatabaseName", databaseName);
		propObj.put("meteringDatabaseUserName", databaseUserName);
		propObj.put("meteringDatabasePassword", databasePassword);
		propObj.put("meteringpoolSize", poolSize);

		
		binder = new ServiceBinder(vertx);
		metering = new MeteringServiceImpl(propObj, vertx);
		
		

		
		consumer = binder.setAddress(METERING_SERVICE_ADDRESS).register(MeteringService.class, metering);

		LOGGER.info("Metering Verticle started");

	}

	@Override
	public void stop() {
		binder.unregister(consumer);
	}
}
