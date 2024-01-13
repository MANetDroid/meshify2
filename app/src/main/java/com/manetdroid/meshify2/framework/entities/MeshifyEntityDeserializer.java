package com.manetdroid.meshify2.framework.entities;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class MeshifyEntityDeserializer extends JsonDeserializer<MeshifyEntity<?>> {

    private static final int ENTITY_TYPE_HANDSHAKE = 0;
    private static final int ENTITY_TYPE_MESSAGE = 1;
    private static final int ENTITY_TYPE_MESH = 2;

    @Override
    public MeshifyEntity<?> deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonNode readTree = jsonParser.getCodec().readTree(jsonParser);
        int entity = ((Integer) readTree.get("entity").numberValue()).intValue();
        JsonNode jsonNode = readTree.get("content");
        jsonNode.fieldNames();
        switch (entity) {
            case ENTITY_TYPE_HANDSHAKE: {
                return new MeshifyEntity<>(entity, new ObjectMapper().treeToValue(jsonNode, MeshifyHandshake.class));
            }
            case ENTITY_TYPE_MESSAGE: {
                return new MeshifyEntity<>(entity, new ObjectMapper().treeToValue(jsonNode, MeshifyContent.class));
            }
            case ENTITY_TYPE_MESH: {
                return new MeshifyEntity<>(entity, new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY).treeToValue(jsonNode, MeshifyForwardTransaction.class));
            }
            default: {
                return null;
            }
        }
    }
}
