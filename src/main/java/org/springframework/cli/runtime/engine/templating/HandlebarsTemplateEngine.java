/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cli.runtime.engine.templating;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HumanizeHelper;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.helper.StringHelpers;

import org.springframework.util.StringUtils;

/**
 * @author Mark Pollack
 */
public class HandlebarsTemplateEngine implements TemplateEngine {

	private final Handlebars handlebars;

	public HandlebarsTemplateEngine() {
		this.handlebars = new Handlebars();
		HumanizeHelper.register(this.handlebars);
		StringHelpers.register(this.handlebars);
	}

	@Override
	public String process(String templateText, Map context) {
		try {
			Template template = this.handlebars.compileInline(templateText);
			if (context == null) {
				context = new HashMap();
			}
			Context handlebarsContext = Context.newBuilder(context).build();
			if (StringUtils.hasText(templateText)) {
				return template.apply(handlebarsContext);
			}
			else {
				return "";
			}
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
