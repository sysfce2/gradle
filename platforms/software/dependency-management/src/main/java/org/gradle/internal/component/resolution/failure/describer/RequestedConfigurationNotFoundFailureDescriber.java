/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.component.resolution.failure.describer;

import com.google.common.collect.ImmutableList;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.api.internal.artifacts.ProjectComponentIdentifierInternal;
import org.gradle.api.internal.attributes.AttributesSchemaInternal;
import org.gradle.internal.component.resolution.failure.exception.ConfigurationSelectionException;
import org.gradle.internal.component.resolution.failure.type.RequestedConfigurationNotFoundFailure;
import org.gradle.util.Path;

import java.util.Optional;

/**
 * A {@link ResolutionFailureDescriber} that describes a {@link RequestedConfigurationNotFoundFailure}.
 */
public abstract class RequestedConfigurationNotFoundFailureDescriber extends AbstractResolutionFailureDescriber<RequestedConfigurationNotFoundFailure> {
    @Override
    public ConfigurationSelectionException describeFailure(RequestedConfigurationNotFoundFailure failure, Optional<AttributesSchemaInternal> schema) {
        String message = buildConfigurationNotFoundFailureMsg(failure);

        ImmutableList.Builder<String> resolutions = ImmutableList.builder();
        boolean isLocalComponent = failure.getRequestedComponentId() instanceof ProjectComponentIdentifier;
        if (isLocalComponent) {
            ProjectComponentIdentifierInternal id = (ProjectComponentIdentifierInternal) failure.getRequestedComponentId();
            Path outgoingVariantsPath = id.getIdentityPath().append(Path.path("outgoingVariants"));
            resolutions.add("To determine which configurations are available in the target project, run " + outgoingVariantsPath.getPath());
        }

        resolutions.addAll(buildResolutions(suggestReviewAlgorithm()));
        return new ConfigurationSelectionException(message, failure, resolutions.build());
    }

    private static String buildConfigurationNotFoundFailureMsg(RequestedConfigurationNotFoundFailure failure) {
        return String.format(
            "A dependency was declared on configuration '%s' of '%s' but no variant with that configuration name exists.",
            failure.getRequestedName(), failure.getRequestedComponentId().getDisplayName()
        );
    }
}
