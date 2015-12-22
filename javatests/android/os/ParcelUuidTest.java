package android.os;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(BlockJUnit4ClassRunner.class)
public class ParcelUuidTest {
    @Test
    public void testConstructor() {
        UUID uuid = UUID.randomUUID();

        ParcelUuid parcelUuid = new ParcelUuid(uuid);

        assertSame(uuid, parcelUuid.getUuid());
    }

    @Test
    public void testParcelable() {
        Parcel parcel = Parcel.obtain();

        ParcelUuid parcelUuid = new ParcelUuid(UUID.randomUUID());

        parcelUuid.writeToParcel(parcel, 0);

        ParcelUuid other = parcel.readTypedObject(ParcelUuid.CREATOR);

        assertEquals(parcelUuid, other);
    }
}
