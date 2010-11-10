/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.torquebox.rails.deployers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.interp.metadata.RubyLoadPathMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.rails.core.RailsRuntimeInitializer;
import org.torquebox.rails.metadata.RailsApplicationMetaData;
import org.torquebox.rack.metadata.RackApplicationMetaData;


/**
 * <pre>
 * Stage: PRE_DESCRIBE
 *    In: RailsApplicationMetaData, RackApplicationMetaData
 *   Out: RubyRuntimeMetaData
 * </pre>
 *
 * Create the Rails runtime initializer and set the extra load paths
 */
public class RailsRubyRuntimeFactoryDescriber extends AbstractDeployer {

	public RailsRubyRuntimeFactoryDescriber() {
		setStage(DeploymentStages.PRE_DESCRIBE);
		setInput(RailsApplicationMetaData.class);
        addInput(RackApplicationMetaData.class);
		addOutput(RubyRuntimeMetaData.class);
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
		if (unit instanceof VFSDeploymentUnit) {
			deploy((VFSDeploymentUnit) unit);
		}
	}

	public void deploy(VFSDeploymentUnit unit) throws DeploymentException {
		log.info( "deploying " + unit );
		RailsApplicationMetaData railsMetaData = unit.getAttachment(RailsApplicationMetaData.class);
		RubyRuntimeMetaData runtimeMetaData = createRuntimeMetaData(unit);
		addRuntimeInitializer(runtimeMetaData, railsMetaData);
	}

    protected RubyRuntimeMetaData createRuntimeMetaData(DeploymentUnit unit) {
		RubyRuntimeMetaData runtimeMetaData = unit.getAttachment(RubyRuntimeMetaData.class);
        assert runtimeMetaData==null : "Not expecting upstream deployer to attach RubyRuntimeMetaData";
        RackApplicationMetaData rackMetaData = unit.getAttachment(RackApplicationMetaData.class);
        assert rackMetaData!=null : "Upstream deployer should've attached RackApplicationMetaData";
        runtimeMetaData = new RubyRuntimeMetaData();
        runtimeMetaData.setBaseDir(rackMetaData.getRackRoot());
        runtimeMetaData.setEnvironment(rackMetaData.getEnvironmentVariables());
        unit.addAttachment(RubyRuntimeMetaData.class, runtimeMetaData);
        return runtimeMetaData;
    }

	protected void addRuntimeInitializer(RubyRuntimeMetaData runtimeMetaData, RailsApplicationMetaData railsMetaData) {
        
		RailsRuntimeInitializer initializer = new RailsRuntimeInitializer(railsMetaData.getRailsRoot(), 
                                                                          railsMetaData.getRailsEnv(), 
                                                                          !railsMetaData.isFrozen(),
                                                                          railsMetaData.getVersionSpec());
		runtimeMetaData.setRuntimeInitializer(initializer);
	}

}
