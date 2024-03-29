/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.common.mailingList;

import java.util.List;

import org.obm.annotations.transactional.Transactional;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.mailingList.MLEmail;
import org.obm.sync.mailingList.MailingList;
import org.obm.sync.services.IMailingList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.utils.LogUtils;

@Singleton
public class MailingListBindingImpl implements IMailingList {

	private static final Logger logger = LoggerFactory
			.getLogger(MailingListBindingImpl.class);

	private MailingListHome mailingListHome;
	
	@Inject
	protected MailingListBindingImpl(MailingListHome mailingListHome) {
		this.mailingListHome = mailingListHome;
	}

	@Override
	@Transactional
	public List<MailingList> listAllMailingList(AccessToken token)
			throws ServerFault {
		try {
			return mailingListHome.findMailingLists(token);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault("error finding addressbooks ");
		}
	}

	@Override
	@Transactional
	public MailingList createMailingList(AccessToken token,
			MailingList mailingList) throws ServerFault {
		try {
			MailingList ml = mailingListHome.createMailingList(
					token, mailingList);

			logger.info(LogUtils.prefix(token) + "Mailing list[" + ml.getId()
					+ "] : " + ml.getName() + " created");
			return ml;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public MailingList modifyMailingList(AccessToken token,
			MailingList mailingList) throws ServerFault {
		try {
			MailingList ml = mailingListHome.modifyMailingList(
					token, mailingList);

			logger.info(LogUtils.prefix(token) + "Mailing list : "
					+ ml.getName() + " modified");
			return ml;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public void removeMailingList(AccessToken token, Integer id)
			throws ServerFault {
		try {
			mailingListHome.removeMailingList(token, id);
			logger.info(LogUtils.prefix(token) + "Mailing list : " + id
					+ " removed");
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public MailingList getMailingListFromId(AccessToken token, Integer id)
			throws ServerFault {
		try {
			return mailingListHome.getMailingListFromId(token, id);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public List<MLEmail> addEmails(AccessToken at, Integer mailingListId,
			List<MLEmail> email) throws ServerFault {
		try {
			List<MLEmail> ret = mailingListHome.addEmails(at, mailingListId, email);
			logger.info(LogUtils.prefix(at) + ret.size() +" emails were added in mailingList: " + mailingListId );
			return ret;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(at) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public void removeEmail(AccessToken at, Integer mailingListId,
			Integer emailId) throws ServerFault {
		try {
			mailingListHome.removeEmail(at, mailingListId,
					emailId);
			logger.info(LogUtils.prefix(at) + "Email[" + emailId
					+ "] in MailingList[" + mailingListId + "]  : "
					+ " removed");
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(at) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}

	}

}
