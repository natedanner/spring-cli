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

package org.springframework.cli.runtime.engine.actions;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Mark Pollack
 */
public class InjectMavenBuildPlugin {

	private final String text;

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public InjectMavenBuildPlugin(@JsonProperty("text") String text) {
		this.text = Objects.requireNonNull(text);
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return "InjectMavenBuildPlugin{" + "text='" + text + '\'' + '}';
	}

}
