
package artefact.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Bucket implements Serializable
{

    @SerializedName("name")
    @Expose
    private String name;
    private final static long serialVersionUID = 434674237350152356L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Bucket() {
    }

    /**
     * 
     * @param name
     */
    public Bucket(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Bucket.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.name == null)? 0 :this.name.hashCode()));
        return result;
    }

    @Override
    public boolean equals(java.lang.Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Bucket) == false) {
            return false;
        }
        Bucket rhs = ((Bucket) other);
        return ((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name)));
    }

}
