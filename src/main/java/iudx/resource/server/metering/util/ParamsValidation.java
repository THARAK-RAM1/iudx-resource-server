package iudx.resource.server.metering.util;

import static iudx.resource.server.apiserver.util.Constants.IUDX_PROVIDER_AUDIT_URL;
import static iudx.resource.server.metering.util.Constants.*;

import io.vertx.core.json.JsonObject;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParamsValidation {

  private static final Logger LOGGER = LogManager.getLogger(ParamsValidation.class);

  public JsonObject paramsCheck(JsonObject request) {

    if (request.getString(ENDPOINT).equals(IUDX_PROVIDER_AUDIT_URL)
        && request.getString(PROVIDER_ID) == null) {
      LOGGER.debug("Info: " + INVALID_PROVIDER_REQUIRED);
      return new JsonObject().put(ERROR, INVALID_PROVIDER_REQUIRED);
    }

    if (request.getString(TIME_RELATION) == null
        || !(request.getString(TIME_RELATION).equals(DURING)
            || request.getString(TIME_RELATION).equals(BETWEEN))) {
      LOGGER.debug("Info: " + TIME_RELATION_NOT_FOUND);
      return new JsonObject().put(ERROR, TIME_RELATION_NOT_FOUND);
    }

    if (request.getString(START_TIME) == null || request.getString(END_TIME) == null) {
      LOGGER.debug("Info: " + TIME_NOT_FOUND);
      return new JsonObject().put(ERROR, TIME_NOT_FOUND);
    }

    if (request.getString(USER_ID) == null || request.getString(USER_ID).isEmpty()) {
      LOGGER.debug("Info: " + USERID_NOT_FOUND);
      request.put(ERROR, USERID_NOT_FOUND);
      return request;
    }
    String iid = request.getString(IID);
    String providerId = request.getString(PROVIDER_ID);
    if (providerId != null && checkProviderId(iid, providerId)) {
      return new JsonObject().put(ERROR, INVALID_PROVIDER_ID);
    }

    // since + is treated as space in uri
    String startTime = request.getString(START_TIME).trim().replaceAll("\\s", "+");
    String endTime = request.getString(END_TIME).trim().replaceAll("\\s", "+");

    ZonedDateTime zdt;
    try {
      zdt = ZonedDateTime.parse(startTime);
      LOGGER.debug("Parsed time: " + zdt.toString());
      zdt = ZonedDateTime.parse(endTime);
      LOGGER.debug("Parsed time: " + zdt.toString());
    } catch (DateTimeParseException e) {
      LOGGER.error("Invalid Date exception: " + e.getMessage());
      return new JsonObject().put(ERROR, INVALID_DATE_TIME);
    }
    ZonedDateTime startZdt = ZonedDateTime.parse(startTime);
    ZonedDateTime endZdt = ZonedDateTime.parse(endTime);

    long zonedDateTimeDayDifference = zonedDateTimeDayDifference(startZdt, endZdt);
    long zonedDateTimeMinuteDifference = zonedDateTimeMinuteDifference(startZdt, endZdt);

    LOGGER.trace(
        "PERIOD between given time day :{} , minutes :{}",
        zonedDateTimeDayDifference,
        zonedDateTimeMinuteDifference);

    if (zonedDateTimeDayDifference < 0 || zonedDateTimeMinuteDifference <= 0) {
      LOGGER.error(INVALID_DATE_DIFFERENCE);
      return new JsonObject().put(ERROR, INVALID_DATE_DIFFERENCE);
    }
    request.put(START_TIME, startTime);
    request.put(END_TIME, endTime);
    return request;
  }

  private long zonedDateTimeDayDifference(ZonedDateTime startTime, ZonedDateTime endTime) {
    return ChronoUnit.DAYS.between(startTime, endTime);
  }

  private long zonedDateTimeMinuteDifference(ZonedDateTime startTime, ZonedDateTime endTime) {
    return ChronoUnit.MINUTES.between(startTime, endTime);
  }

  private boolean checkProviderId(String iid, String providerId) {
    return !iid.substring(0, iid.indexOf('/', iid.indexOf('/') + 1)).equals(providerId);
  }
}
