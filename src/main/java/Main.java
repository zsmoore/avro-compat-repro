import avro.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;

public class Main {

    /**
     * The same schema as User.avsc with an optional email field added to simulate
     * forward compatibility scenario without updated compiled specific records
     */
    private static final Schema UPDATED_SCHEMA_WITH_OPTIONAL = new Schema.Parser().parse("""
            {
                "type": "record",
                "name": "User",
                "namespace": "avro",
                "fields": [
                  {
                    "name": "firstName",
                    "type": "string"
                  },
                  {
                    "name": "lastName",
                    "type": "string"
                  },
                  {
                    "name": "phoneNumber",
                    "type": "string"
                  },
                  {
                    "name": "email",
                    "type": ["null", "string"],
                    "default": null
                  }
                ]
            }""");


    /**
     * JSON based data which is compliant with original User.avsc
     */
    private static final String ORIGINAL_SCHEMA_COMPLIANT_JSON = """
            {
              "firstName": "Test",
              "lastName": "User",
              "phoneNumber": "someNumber"
            }""";


    private static IndexedRecord manualParseUser() throws Exception {
        // generic record
        GenericData.Record record = new GenericData.Record(UPDATED_SCHEMA_WITH_OPTIONAL);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode obj = mapper.readTree(ORIGINAL_SCHEMA_COMPLIANT_JSON);
        Schema.Field firstName = record.getSchema().getField("firstName");
        record.put(firstName.pos(), obj.get("firstName").asText());

        Schema.Field lastName = record.getSchema().getField("lastName");
        record.put(lastName.pos(), obj.get("lastName").asText());

        Schema.Field phoneNumber = record.getSchema().getField("phoneNumber");
        record.put(phoneNumber.pos(), obj.get("phoneNumber").asText());

        // Manually insert default null for email
        Schema.Field email = record.getSchema().getField("email");
        record.put(email.pos(), null);

        return record;
    }
    public static void main(String[] args) throws Exception {
        IndexedRecord record = manualParseUser();

        // Throw as User will end up hitting GenericData.put which will throw for unknown fields as we use the
        // runtime schema but the specific record class is not equipped to handle the updated schema since it is
        // compiled on an old version
        User specificUser = (User) SpecificData.get().deepCopy(record.getSchema(), record);
    }
}