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
package org.xwiki.rendering.internal.macro.python;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.macro.script.AbstractJSR223ScriptMacro;
import org.xwiki.rendering.macro.script.JSR223ScriptMacroParameters;

/**
 * A macro for executing ruby scripts.
 * 
 * @version $Id$
 * @since 2.0M2
 */
@Component
@Named("python")
@Singleton
public class PythonMacro extends AbstractJSR223ScriptMacro<JSR223ScriptMacroParameters>
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION = "Executes a python script.";

    /**
     * The description of the macro content.
     */
    private static final String CONTENT_DESCRIPTION = "The python script to execute";

    /**
     * Create and initialize the descriptor of the macro.
     */
    public PythonMacro()
    {
        super("Python", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION));
    }

    @Override
    protected Object eval(String content, ScriptEngine engine, ScriptContext scriptContext) throws ScriptException
    {
        // Workaround a bug in Jython which only support ASCII in the JSR223 filename property
        // See https://github.com/jythontools/jython/issues/170
        String filename = (String) scriptContext.getAttribute(ScriptEngine.FILENAME);
        if (filename != null) {
            StringBuilder builder = null;
            final int length = filename.length();
            for (int i = 0; i < length; i++) {
                char c = filename.charAt(i);
                if (c >= 0x100) {
                    if (builder == null) {
                        builder = new StringBuilder();
                        builder.append(filename.substring(0, i));
                    }
                    builder.append('_');
                } else if (builder != null) {
                    builder.append(c);
                }
            }

            if (builder != null) {
                scriptContext.setAttribute(ScriptEngine.FILENAME, builder.toString(), ScriptContext.ENGINE_SCOPE);
            }
        }

        return super.eval(content, engine, scriptContext);
    }
}
