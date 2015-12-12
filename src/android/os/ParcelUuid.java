package android.os;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Stein Eldar Johnsen
 * @since 12.12.15.
 */
public class ParcelUuid implements Parcelable {
    public ParcelUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public static ParcelUuid fromString(String uuid) {
        return new ParcelUuid(UUID.fromString(uuid));
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ParcelUuid)) return false;
        ParcelUuid other = (ParcelUuid) o;
        return Objects.equals(uuid, other.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Override
    public String toString() {
        return Objects.toString(uuid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(toString());
    }

    public static final Creator<ParcelUuid> CREATOR = new Creator<ParcelUuid>() {
        @Override
        public ParcelUuid createFromParcel(Parcel source) {
            return fromString(source.readString());
        }

        @Override
        public ParcelUuid[] newArray(int size) {
            return new ParcelUuid[size];
        }
    };

    private final UUID uuid;
}
