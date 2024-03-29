package org.obm.push.backend;

import java.util.Set;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.SyncCollection;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.protocol.provisioning.Policy;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;

public interface IBackend {

	String getWasteBasket();

	Policy getDevicePolicy(BackendSession bs);

	/**
	 * Push support
	 * 
	 * @param ccl
	 * @return a registration that the caller can use to cancel monitor of a
	 *         ressource
	 */
	IListenerRegistration addChangeListener(ICollectionChangeListener ccl);

	void startEmailMonitoring(BackendSession bs, Integer collectionId) throws CollectionNotFoundException;

	void resetCollection(BackendSession bs, Integer collectionId) throws DaoException;

	AccessToken login(String loginAtDomain, String password) throws AuthFault;

	Set<SyncCollection> getChangesSyncCollections(CollectionChangeListener collectionChangeListener) 
			throws DaoException, CollectionNotFoundException, UnknownObmSyncServerException, ProcessingEmailException;
	
}
