package edu.carleton.comp4601.crawler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Marshaller {

	// return a byte array representing the serialized version of an object

	public static byte[] serializeObject(Object o) throws IOException {
		if (o == null) {
			return null;
		}
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
		objectStream.writeObject(o);
		objectStream.close();

		return byteStream.toByteArray();
	}

	// return an object which has been deserialized from a byte array

	public static Object deserializeObject(byte[] data) throws IOException {
		if (data == null || data.length < 1) {
			return null;
		}
		Object object = null;
		try {
			ObjectInputStream objectStream = new ObjectInputStream(new ByteArrayInputStream(data));
			object = objectStream.readObject();
			objectStream.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return object;
	}
}
