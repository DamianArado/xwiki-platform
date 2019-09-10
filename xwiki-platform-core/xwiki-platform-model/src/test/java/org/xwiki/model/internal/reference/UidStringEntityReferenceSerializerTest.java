/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.xwiki.model.internal.reference;

import java.util.Arrays;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link UidStringEntityReferenceSerializer}.
 *
 * @version $Id$
 * @since 3.5RC1
 */
@ComponentTest
@ComponentList({
    DefaultSymbolScheme.class
})
public class UidStringEntityReferenceSerializerTest
{
    @InjectMockComponents
    private DefaultStringEntityReferenceResolver resolver;

    private EntityReferenceSerializer<String> serializer;

    @BeforeEach
    public void setUp() throws Exception
    {
        this.serializer = new UidStringEntityReferenceSerializer();
    }

    @Test
    public void serializeDocumentReference()
    {
        EntityReference reference = resolver.resolve("wiki:space.page", EntityType.DOCUMENT);
        assertEquals("4:wiki5:space4:page", serializer.serialize(reference));

        reference = resolver.resolve("wiki1.wiki2:wiki3:some.space.page", EntityType.DOCUMENT);
        assertEquals("17:wiki1.wiki2:wiki34:some5:space4:page", serializer.serialize(reference));

        // Verify that passing null doesn't throw a NPE
        assertNull(serializer.serialize(null));
    }

    @Test
    public void serializeDocumentReferenceWithLocale()
    {
        EntityReference reference = new DocumentReference("wiki", "space", "page", Locale.US);
        assertEquals("4:wiki5:space4:page5:en_US", serializer.serialize(reference));

        reference = new DocumentReference("wiki1.wiki2:wiki3", Arrays.asList("some", "space"), "page", Locale.US);
        assertEquals("17:wiki1.wiki2:wiki34:some5:space4:page5:en_US", serializer.serialize(reference));
    }

    @Test
    public void serializeSpaceReference()
    {
        EntityReference reference = resolver.resolve("wiki:space1.space2", EntityType.SPACE);
        assertEquals("4:wiki6:space16:space2", serializer.serialize(reference));
    }

    @Test
    public void serializeAttachmentReference()
    {
        EntityReference reference = resolver.resolve("wiki:space.page@filename", EntityType.ATTACHMENT);
        assertEquals("4:wiki5:space4:page8:filename", serializer.serialize(reference));
    }

    @Test
    public void serializeReferenceWithChild()
    {
        EntityReference reference = resolver.resolve("wiki:Space.Page", EntityType.DOCUMENT);
        assertEquals("4:wiki5:Space", serializer.serialize(reference.getParent()));

        assertEquals("4:wiki", serializer.serialize(reference.getParent().getParent()));
    }

    /**
     * Tests resolving and re-serializing an object reference.
     */
    @Test
    public void serializeObjectReference()
    {
        EntityReference reference = resolver.resolve("wiki:space.page^wiki:space.class[0]", EntityType.OBJECT);
        assertEquals("4:wiki5:space4:page19:wiki:space.class[0]", serializer.serialize(reference));

        reference = resolver.resolve("wiki:space.page^xwiki:space.class[0]", EntityType.OBJECT);
        assertEquals("4:wiki5:space4:page20:xwiki:space.class[0]", serializer.serialize(reference));

        // test escaping character
        reference = resolver.resolve("wiki:space.page^wiki:space.cla\\^ss[0]", EntityType.OBJECT);
        assertEquals("4:wiki5:space4:page20:wiki:space.cla^ss[0]", serializer.serialize(reference));

        reference = resolver.resolve("wiki:spa^ce.page^wiki:space.cla\\^ss[0]", EntityType.OBJECT);
        assertEquals("4:wiki6:spa^ce4:page20:wiki:space.cla^ss[0]", serializer.serialize(reference));
    }

    /**
     * Tests resolving and re-serializing an property reference.
     */
    @Test
    public void serializeObjectPropertyReference()
    {
        EntityReference reference =
            resolver.resolve("wiki:space.page^wiki:space.class[0].prop", EntityType.OBJECT_PROPERTY);
        assertEquals("4:wiki5:space4:page19:wiki:space.class[0]4:prop", serializer.serialize(reference));

        reference = resolver.resolve("wiki:space.page^xwiki:space.class[0].prop", EntityType.OBJECT_PROPERTY);
        assertEquals("4:wiki5:space4:page20:xwiki:space.class[0]4:prop", serializer.serialize(reference));

        // test escaping character
        reference = resolver.resolve("wiki:space.page^wiki:space.class[0].prop\\.erty", EntityType.OBJECT_PROPERTY);
        assertEquals("4:wiki5:space4:page19:wiki:space.class[0]9:prop.erty", serializer.serialize(reference));
    }

    /**
     * Tests resolving and re-serializing an class property reference.
     */
    @Test
    public void serializeClassPropertyReference()
    {
        EntityReference reference = resolver.resolve("wiki:space.page^ClassProperty", EntityType.CLASS_PROPERTY);
        assertEquals("4:wiki5:space4:page13:ClassProperty", serializer.serialize(reference));

        // test escaping character
        reference = resolver.resolve("wiki:space.page^ClassPro\\^perty", EntityType.CLASS_PROPERTY);
        assertEquals("4:wiki5:space4:page14:ClassPro^perty", serializer.serialize(reference));
    }

    @Test
    public void serializeRelativeReference()
    {
        EntityReference reference = new EntityReference("page", EntityType.DOCUMENT);
        assertEquals("4:page", serializer.serialize(reference));

        reference = new EntityReference("page", EntityType.DOCUMENT, new EntityReference("space", EntityType.SPACE));
        assertEquals("5:space4:page", serializer.serialize(reference));
    }
}
