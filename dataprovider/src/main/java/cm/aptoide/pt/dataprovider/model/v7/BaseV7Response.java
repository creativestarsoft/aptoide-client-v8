/*
 * Copyright (c) 2016.
 * Modified on 04/08/2016.
 */

package cm.aptoide.pt.dataprovider.model.v7;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

/**
 * Created by neuro on 20-04-2016.
 */
@Data public class BaseV7Response {

  private Info info;
  private List<Error> errors;

  public Error getError() {
    if (errors != null && errors.size() > 0) {
      return errors.get(0);
    } else {
      return null;
    }
  }

  public boolean isOk() {
    return info != null && info.getStatus() == Info.Status.OK;
  }

  public enum Type {
    FACEBOOK_1, FACEBOOK_2, TWITCH_1, TWITCH_2, TWITTER_1, TWITTER_2, YOUTUBE_1, YOUTUBE_2
  }

  @Data public static class Info {

    private Status status;
    private Time time;

    public Info() {
    }

    public Info(Status status, Time time) {
      this.status = status;
      this.time = time;
    }

    public enum Status {
      OK, QUEUED, FAIL, Processing
    }

    @Data public static class Time {

      private double seconds;
      private String human;
    }
  }

  @Data public static class Error {

    private String code;
    private String description;
    private Details details;
  }

  @Data public static class Details {
    //Is only necessary for store/set requests and only appears with a STORE-9 error
    @JsonProperty("store_links") private List<StoreLinks> storeLinks;
  }

  @Data public static class StoreLinks {
    private Type type;
    private String url;
    private String error;
  }
}
