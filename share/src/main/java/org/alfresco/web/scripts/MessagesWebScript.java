/*
 * #%L
 * Alfresco Share WAR
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

/*
* There is an Enterprise overlay for this file
*/

package org.alfresco.web.scripts;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.alfresco.web.site.EditionInfo;
import org.alfresco.web.site.EditionInterceptor;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.StringBuilderWriter;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.json.JSONWriter;

/**
 * WebScript responsible for returning a JavaScript response containing a JavaScript
 * associative array of all I18N messages name/key pairs installed on the web-tier.
 * <p>
 * The JavaScript object is created as 'Alfresco.messages' - example usage:
 * <code>
 * var msg = Alfresco.messages["messageid"];
 * </code>
 *
 * @author Kevin Roast
 */
public class MessagesWebScript extends org.springframework.extensions.webscripts.MessagesWebScript
{
    /**
     * Generate the message for a given locale.
     *
     * @param locale    Java locale format
     *
     * @return messages as JSON string
     *
     * @throws IOException
     */
    @Override
    protected String generateMessages(WebScriptRequest req, WebScriptResponse res, String locale) throws IOException
    {
        Writer writer = new StringBuilderWriter(8192);
        writer.write("if (typeof Alfresco == \"undefined\" || !Alfresco) {var Alfresco = {};}\r\n");
        writer.write("Alfresco.messages = Alfresco.messages || {global: null, scope: {}}\r\n");
        writer.write("Alfresco.messages.global = ");
        JSONWriter out = new JSONWriter(writer);
        try
        {
            out.startObject();
            Map<String, String> messages = I18NUtil.getAllMessages(I18NUtil.parseLocale(locale));
            for (Map.Entry<String, String> entry : messages.entrySet())
            {
                out.writeValue(entry.getKey(), entry.getValue());
            }
            out.endObject();
        }
        catch (IOException jsonErr)
        {
            throw new WebScriptException("Error building messages response.", jsonErr);
        }
        writer.write(";\r\n");

/*
        if (isCommunity())
        {
            // community logo
            final String serverPath = req.getServerPath();
            final int schemaIndex = serverPath.indexOf(':');
            writer.write("window.setTimeout(function(){(document.getElementById('alfresco-yuiloader')||document.createElement('div')).innerHTML = '<img src=\"");
            writer.write(serverPath.substring(0, schemaIndex));
            writer.write("://www.alfresco.com/assets/images/logos/community-5.2-share.png\" alt=\"*\" style=\"display:none\"/>\'}, 100);\r\n");
        }
*/
        return writer.toString();
    }

    @Override
    protected String getMessagesPrefix(WebScriptRequest req, WebScriptResponse res, String locale) throws IOException
    {
        return "if (typeof Alfresco == \"undefined\" || !Alfresco) {var Alfresco = {};}\r\nAlfresco.messages = Alfresco.messages || {global: null, scope: {}}\r\nAlfresco.messages.global = ";
    }

    @Override
    protected String getMessagesSuffix(WebScriptRequest req, WebScriptResponse res, String locale) throws IOException
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append(";\r\n");

/*
        if (isCommunity())
        {
            // community logo
            final String serverPath = req.getServerPath();
            final int schemaIndex = serverPath.indexOf(':');
            sb.append("window.setTimeout(function(){(document.getElementById('alfresco-yuiloader')||document.createElement('div')).innerHTML = '<img src=\"");
            sb.append(serverPath.substring(0, schemaIndex));
            sb.append("://www.alfresco.com/assets/images/logos/community-5.2-share.png\" alt=\"*\" style=\"display:none\"/>\'}, 100);\r\n");
        }
*/
        return sb.toString();
    }
    
    protected boolean isLicensed()
    {
        boolean licensed = false;
        final RequestContext rc = ThreadLocalRequestContext.getRequestContext();
        if (rc != null)
        {
            final String edition = ((EditionInfo)rc.getValue(EditionInterceptor.EDITION_INFO)).getEdition();
            licensed = (EditionInterceptor.ENTERPRISE_EDITION.equals(edition));
        }
        return licensed;
    }
    
    /**
     * Verifies if the licence edition is community
     * @return true if the edition was successfuly retrieved and is UNKNOWN_EDITION , false otherwise
     */
    private boolean isCommunity()
    {
        final RequestContext rc = ThreadLocalRequestContext.getRequestContext();
        if (rc != null)
        {
            EditionInfo editionInfo = ((EditionInfo)rc.getValue(EditionInterceptor.EDITION_INFO));
            if (editionInfo.getValidResponse()) 
            { 
                return EditionInterceptor.UNKNOWN_EDITION.equals(editionInfo.getEdition()); 
            }
            //else, could not retrieve licence info from server
        }
        return false;
    }
}
