package net.peerindex.geocoder;



import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Enno Shioji (eshioji@gmail.com)
 */
public class Location implements Serializable {
    private static final long serialVersionUID = -2981641839995703203L;

    private  int geonameId;
    private FeatureCodeCategory featureCodeCategory;
    private  String defaultName;
    private  String featureCode;
    private  Map<FeatureCodeCategory,String> codes = new HashMap<>();
    private Set<String> names;

    private  Long population;
    private  Double weight;

    private double lat;
    private double lng;


    public Location() {

    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }


    public FeatureCodeCategory getFeatureCodeCategory() {
        return featureCodeCategory;
    }

    Double getWeight() {
        return weight;
    }

    void setWeight(Double weight) {
        this.weight = weight;
    }

    public Map<FeatureCodeCategory,String> getCodes(){
        return codes;
    }

    public int getGeonameId() {
        return geonameId;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public String getFeatureCode() {
        return featureCode;
    }

    public void setGeonameId(int geonameId) {
        this.geonameId = geonameId;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public void setFeatureCode(String featureCode) {
        this.featureCode = featureCode;
    }

    public void setCodes(Map<FeatureCodeCategory, String> codes) {
        this.codes = codes;
    }

    public void setPopulation(Long population) {
        this.population = population;
    }

    public void setFeatureCodeCategory(FeatureCodeCategory featureCodeCategory) {
        this.featureCodeCategory = featureCodeCategory;
    }

    public Long getPopulation() {
        return population;
    }

    public void setNames(Set<String> names) {
        this.names = names;
    }

    public Set<String> getNames() {
        return names;
    }

    public void addCode(FeatureCodeCategory category, String code) {
        this.codes.put(category, code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (geonameId != location.geonameId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return geonameId;
    }

    @Override
    public String toString() {
        return "Location{" +
                "geonameId=" + geonameId +
                ", defaultName='" + defaultName + '\'' +
                ", featureCode='" + featureCode + '\'' +
                ", codes=" + codes +
                ", population=" + population +
                ", weight=" + weight +
                ", lat=" + lat +
                ", lng=" + lng +
                ", featureCodeCategory=" + featureCodeCategory +
                '}';
    }

}
