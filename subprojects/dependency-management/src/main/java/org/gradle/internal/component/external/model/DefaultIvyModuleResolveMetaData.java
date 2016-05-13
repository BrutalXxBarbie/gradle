/*
 * Copyright 2014 the original author or authors.
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
package org.gradle.internal.component.external.model;

import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier;
import org.gradle.api.internal.artifacts.ivyservice.IvyUtil;
import org.gradle.api.internal.artifacts.ivyservice.NamespaceId;
import org.gradle.internal.component.model.IvyArtifactName;

import java.util.Map;
import java.util.Set;

public class DefaultIvyModuleResolveMetaData extends AbstractModuleComponentResolveMetaData implements IvyModuleResolveMetaData {
    private final Map<NamespaceId, String> extraInfo;
    private final String branch;

    public DefaultIvyModuleResolveMetaData(ModuleComponentIdentifier componentIdentifier, Set<IvyArtifactName> artifacts) {
        this(componentIdentifier, new ModuleDescriptorState(IvyUtil.createModuleDescriptor(componentIdentifier, artifacts)));
    }

    public DefaultIvyModuleResolveMetaData(ModuleComponentIdentifier componentIdentifier, ModuleDescriptorState moduleDescriptor) {
        this(componentIdentifier, DefaultModuleVersionIdentifier.newId(componentIdentifier), moduleDescriptor);
    }

    private DefaultIvyModuleResolveMetaData(ModuleComponentIdentifier componentIdentifier, ModuleVersionIdentifier identifier, ModuleDescriptorState moduleDescriptor) {
        super(componentIdentifier, identifier, moduleDescriptor);
        this.extraInfo = moduleDescriptor.getExtraInfo();
        this.branch = moduleDescriptor.getBranch();
    }

    @Override
    public DefaultIvyModuleResolveMetaData copy() {
        // TODO:ADAM - need to make a copy of the descriptor (it's effectively immutable at this point so it's not a problem yet)
        DefaultIvyModuleResolveMetaData copy = new DefaultIvyModuleResolveMetaData(getComponentId(), getId(), getDescriptor());
        copyTo(copy);
        return copy;
    }

    public String getBranch() {
        return branch;
    }

    public Map<NamespaceId, String> getExtraInfo() {
        return extraInfo;
    }
}
