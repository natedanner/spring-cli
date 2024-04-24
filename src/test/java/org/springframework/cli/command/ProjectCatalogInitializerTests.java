/*
 * Copyright 2021 the original author or authors.
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

package org.springframework.cli.command;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cli.config.ProjectCatalogInitializer;
import org.springframework.cli.config.SpringCliProjectCatalogProperties;
import org.springframework.cli.config.SpringCliUserConfig;
import org.springframework.cli.config.SpringCliUserConfig.ProjectCatalog;
import org.springframework.cli.config.SpringCliUserConfig.ProjectCatalogs;
import org.springframework.cli.git.GitSourceRepositoryService;
import org.springframework.cli.git.SourceRepositoryService;
import org.springframework.cli.testutil.TableAssertions;
import org.springframework.cli.util.TerminalMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.table.Table;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectCatalogInitializerTests {

	@Test
	void testHasInitializedProjectCatalog() {
		ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(ProjectCatalogInitializerTests.ProjectCatalogInitializerConfig.class);
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(ProjectCatalogCommands.class);
			ProjectCatalogCommands projectCatalogCommands = context.getBean(ProjectCatalogCommands.class);
			Table table = (Table) projectCatalogCommands.catalogList(false);
			assertThat(table.getModel().getRowCount()).isEqualTo(2);
			TableAssertions.verifyTableValue(table, 1, 0, "gs");
			TableAssertions.verifyTableValue(table, 1, 1, "Getting Started Catalog");
			TableAssertions.verifyTableValue(table, 1, 2, "https://github.com/rd-1-2022/spring-gs-catalog");
			TableAssertions.verifyTableValue(table, 1, 3, "[java-17, boot-3.1]");
		});
	}

	@Test
	void testPropertyOverride() {
		ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(ProjectCatalogInitializerTests.ProjectCatalogInitializerConfig.class)
			.withPropertyValues("spring.cli.project.catalog.init.name=myname",
					"spring.cli.project.catalog.init.url=myurl",
					"spring.cli.project.catalog.init.description=mydescription",
					"spring.cli.project.catalog.init.tags=tag1,tag2");
		// //The default values should not be configured, but taken instead from property
		// values
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(ProjectCatalogCommands.class);
			ProjectCatalogCommands projectCatalogCommands = context.getBean(ProjectCatalogCommands.class);
			Table table = (Table) projectCatalogCommands.catalogList(false);
			assertThat(table.getModel().getRowCount()).isEqualTo(2);
			TableAssertions.verifyTableValue(table, 1, 0, "myname");
			TableAssertions.verifyTableValue(table, 1, 1, "mydescription");
			TableAssertions.verifyTableValue(table, 1, 2, "myurl");
			TableAssertions.verifyTableValue(table, 1, 3, "[tag1, tag2]");
		});
	}

	@Test
	void testWithExistingProjectCatalog() {
		ApplicationContextRunner contextRunner = new ApplicationContextRunner().withUserConfiguration(
				ProjectCatalogInitializerTests.ProjectCatalogInitializerWithExistingCatalogConfig.class);
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(ProjectCatalogCommands.class);
			ProjectCatalogCommands projectCatalogCommands = context.getBean(ProjectCatalogCommands.class);
			Table table = (Table) projectCatalogCommands.catalogList(false);
			assertThat(table.getModel().getRowCount()).isEqualTo(2);
			TableAssertions.verifyTableValue(table, 1, 0, "fooname");
			TableAssertions.verifyTableValue(table, 1, 1, "foodescription");
			TableAssertions.verifyTableValue(table, 1, 2, "foourl");
			TableAssertions.verifyTableValue(table, 1, 3, "[footag1, footag2]");
		});
	}

	@Configuration
	@EnableConfigurationProperties(SpringCliProjectCatalogProperties.class)
	static class ProjectCatalogInitializerConfig {

		@Bean
		SpringCliUserConfig springCliUserConfig() {
			FileSystem fileSystem = Jimfs.newFileSystem();
			Function<String, Path> pathProvider = fileSystem::getPath;
			return new SpringCliUserConfig(pathProvider);
		}

		@Bean
		GitSourceRepositoryService gitSourceRepositoryService(SpringCliUserConfig springCliUserConfig) {
			return new GitSourceRepositoryService(springCliUserConfig);
		}

		@Bean
		ObjectMapper objectMapper() {
			return new ObjectMapper();
		}

		@Bean
		ProjectCatalogCommands projectCatalogCommands(SpringCliUserConfig springCliUserConfig,
				SourceRepositoryService sourceRepositoryService, ObjectMapper objectMapper) {
			return new ProjectCatalogCommands(springCliUserConfig, sourceRepositoryService, TerminalMessage.noop(),
					objectMapper);
		}

		@Bean
		ProjectCatalogInitializer projectCatalogInitializer(SpringCliUserConfig springCliUserConfig,
				SpringCliProjectCatalogProperties springCliProjectCatalogProperties) {
			return new ProjectCatalogInitializer(springCliUserConfig, springCliProjectCatalogProperties);
		}

	}

	@Configuration
	@EnableConfigurationProperties(SpringCliProjectCatalogProperties.class)
	static class ProjectCatalogInitializerWithExistingCatalogConfig {

		@Bean
		SpringCliUserConfig springCliUserConfig() {
			FileSystem fileSystem = Jimfs.newFileSystem();
			Function<String, Path> pathProvider = fileSystem::getPath;
			return new SpringCliUserConfig(pathProvider);
		}

		@Bean
		SourceRepositoryService gitSourceRepositoryService(SpringCliUserConfig springCliUserConfig) {
			return new GitSourceRepositoryService(springCliUserConfig);
		}

		@Bean
		ObjectMapper objectMapper() {
			return new ObjectMapper();
		}

		@Bean
		ProjectCatalogCommands projectCatalogCommands(SpringCliUserConfig springCliUserConfig,
				SourceRepositoryService sourceRepositoryService, ObjectMapper objectMapper) {
			List<ProjectCatalog> projectCatalogList = new ArrayList<>();
			projectCatalogList
				.add(ProjectCatalog.of("fooname", "foodescription", "foourl", Arrays.asList("footag1, footag2")));

			// Save to disk
			ProjectCatalogs projectCatalogs = new ProjectCatalogs();
			projectCatalogs.setProjectCatalogs(projectCatalogList);
			springCliUserConfig.setProjectCatalogs(projectCatalogs);

			return new ProjectCatalogCommands(springCliUserConfig, sourceRepositoryService, TerminalMessage.noop(),
					objectMapper);
		}

		@Bean
		ProjectCatalogInitializer projectCatalogInitializer(SpringCliUserConfig springCliUserConfig,
				SpringCliProjectCatalogProperties springCliProjectCatalogProperties) {
			return new ProjectCatalogInitializer(springCliUserConfig, springCliProjectCatalogProperties);
		}

	}

}
