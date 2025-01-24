package com.android.gpslocation.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@Keep
public class GeoCodeResponse {

@SerializedName("hits")
@Expose
private List<Hit> hits = null;
@SerializedName("took")
@Expose
private Integer took;

public List<Hit> getHits() {
return hits;
}

public void setHits(List<Hit> hits) {
this.hits = hits;
}

public Integer getTook() {
return took;
}

public void setTook(Integer took) {
this.took = took;
}

}