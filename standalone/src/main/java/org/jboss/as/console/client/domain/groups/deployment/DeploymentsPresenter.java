/*
 * JBoss, Home of Professional Open Source
 * Copyright <YEAR> Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package org.jboss.as.console.client.domain.groups.deployment;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.core.NameTokens;
import org.jboss.as.console.client.core.SuspendableView;
import org.jboss.as.console.client.core.message.Message;
import org.jboss.as.console.client.domain.groups.ServerGroupMgmtPresenter;
import org.jboss.as.console.client.domain.model.EntityFilter;
import org.jboss.as.console.client.domain.model.Predicate;
import org.jboss.as.console.client.domain.model.ServerGroupRecord;
import org.jboss.as.console.client.domain.model.ServerGroupStore;
import org.jboss.as.console.client.domain.model.SimpleCallback;
import org.jboss.as.console.client.shared.model.DeploymentRecord;
import org.jboss.as.console.client.shared.model.DeploymentStore;

import java.util.List;

/**
 * @author Heiko Braun
 * @date 3/1/11
 */
public class DeploymentsPresenter extends Presenter<DeploymentsPresenter.MyView, DeploymentsPresenter.MyProxy> {

    private final PlaceManager placeManager;
    private DeploymentStore deploymentStore;
    private ServerGroupStore serverGroupStore;

    private String groupFilter = "";
    private String typeFilter= "";

    private EntityFilter<DeploymentRecord> filter = new EntityFilter<DeploymentRecord>();

    public List<DeploymentRecord> deployments;


    @ProxyCodeSplit
    @NameToken(NameTokens.DeploymentsPresenter)
    public interface MyProxy extends Proxy<DeploymentsPresenter>, Place {
    }

    public interface MyView extends SuspendableView {
        void setPresenter(DeploymentsPresenter presenter);
        void updateDeployments(List<DeploymentRecord> deploymentRecords);
        void updateGroups(List<ServerGroupRecord> serverGroupRecords);
    }

    @Inject
    public DeploymentsPresenter(
            EventBus eventBus, MyView view, MyProxy proxy,
            PlaceManager placeManager,
            DeploymentStore deploymentStore,
            ServerGroupStore serverGroupStore) {
        super(eventBus, view, proxy);

        this.placeManager = placeManager;
        this.deploymentStore = deploymentStore;
        this.serverGroupStore = serverGroupStore;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }


    @Override
    protected void onReset() {
        super.onReset();

        deploymentStore.loadDeployments(new SimpleCallback<List<DeploymentRecord>>() {

            @Override
            public void onSuccess(List<DeploymentRecord> result) {
                deployments = result;
                getView().updateDeployments(result);
            }
        });

        serverGroupStore.loadServerGroups(new SimpleCallback<List<ServerGroupRecord>>() {
            @Override
            public void onSuccess(List<ServerGroupRecord> result) {
                getView().updateGroups(result);
            }
        });
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(getEventBus(), ServerGroupMgmtPresenter.TYPE_MainContent, this);
    }

    public void deleteDeployment(DeploymentRecord deploymentRecord) {
        deploymentStore.deleteDeployment(deploymentRecord, new SimpleCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {

            }
        });
    }


    public void onFilterGroup(final String groupName) {
        this.groupFilter = groupName;
        getView().updateDeployments(
                filter.apply(new TypeAndGroupPredicate(), deployments)
        );
    }

    public void onFilterType(final String type) {

        this.typeFilter = type;
        getView().updateDeployments(
                filter.apply(new TypeAndGroupPredicate(), deployments)
        );
    }

    class TypeAndGroupPredicate implements Predicate<DeploymentRecord>
    {
        @Override
        public boolean appliesTo(DeploymentRecord candidate) {


            boolean groupMatch = groupFilter.equals("") ?
                    true : candidate.getServerGroup().equals(groupFilter);

            boolean typeMatch = typeFilter.equals("") ?
                    true : candidate.getName().endsWith(typeFilter);

            return groupMatch && typeMatch;
        }
    }

    public void launchNewDeploymentDialoge() {
        Console.MODULES.getMessageCenter().notify(
                new Message("Not implemented yet", Message.Severity.Warning)
        );
    }

}
