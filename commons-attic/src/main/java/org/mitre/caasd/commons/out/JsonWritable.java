package org.mitre.caasd.commons.out;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * Interface for converting a record to a json object that can easily be written out
 */
public interface JsonWritable {

    // THIS METHOD MIGHT BE REMOVED -- WE NEED TO SEE IF GETTING ACCESS TO THE JsonElement IS BETTER
    default JsonElement getJsonElement() {
        Gson gson = new GsonBuilder().create();
        return gson.toJsonTree(this, this.getClass());
    }

    default String asJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this);

        return checkNotNull(json, "Gson produced null when attempting to convert an object to JSON");
    }
}
