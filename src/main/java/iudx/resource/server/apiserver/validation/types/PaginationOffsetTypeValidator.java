package iudx.resource.server.apiserver.validation.types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PaginationOffsetTypeValidator implements Validator {

  private static final Logger LOGGER = LogManager.getLogger(PaginationOffsetTypeValidator.class);

  private final String value;
  private final boolean required;

  public PaginationOffsetTypeValidator(String value, boolean required) {
    this.value = value;
    this.required = required;
  }

  @Override
  public boolean isValid() {
    if (required && (value == null || value.isBlank())) {
      LOGGER.error("Validation error : null or blank value for required mandatory field");
      return false;
    } else {
      if (value == null) {
        return true;
      }
      if (value.isBlank()) {
        LOGGER.error("Validation error :  blank value passed");
        return false;
      }
    }
    if (!isValidValue(value)) {
      LOGGER.error("Validation error : invalid pagination offset Value [ " + value + " ]");
      return false;
    }
    return true;
  }

  private boolean isValidValue(String value) {
    try {
      int offset = Integer.parseInt(value);
      // TODO : Need to fix this after carefully considering different values which will not effect
      // the elastic performance

      if (offset > 50000 || offset < 0) {
        LOGGER.error(
            "Validation error : invalid pagination offset Value > 50000 or negative value passed [ "
                + value + " ]");
        return false;
      }
      return true;
    } catch (Exception ex) {
      LOGGER.error("Validation error : invalid pagination offset Value [ " + value
          + " ] only integer expected");
      return false;
    }
  }

  @Override
  public int failureCode() {
    return 400;
  }

  @Override
  public String failureMessage() {
    return "bad query";
  }

}

