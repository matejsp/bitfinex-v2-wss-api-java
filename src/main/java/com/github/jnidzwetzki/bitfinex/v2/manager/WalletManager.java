/*******************************************************************************
 *
 *    Copyright (C) 2015-2018 Jan Kristof Nidzwetzki
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License. 
 *
 *******************************************************************************/
package com.github.jnidzwetzki.bitfinex.v2.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiCallbackRegistry;
import com.github.jnidzwetzki.bitfinex.v2.commands.CalculateCommand;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.Wallet;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class WalletManager extends AbstractManager {

	/**
	 * Wallets
	 *
	 *  Currency, Wallet-Type, Wallet
	 */
	private final Table<String, String, Wallet> walletTable;

	public WalletManager(final BitfinexApiBroker bitfinexApiBroker, final ExecutorService executorService, BitfinexApiCallbackRegistry callbackRegistry) {
		super(bitfinexApiBroker, executorService);
		this.walletTable = HashBasedTable.create();
		callbackRegistry.onWalletsEvent(wallets -> wallets.forEach(wallet -> {
            try {
                Table<String, String, Wallet> walletTable = getWalletTable();
                synchronized (walletTable) {
                    walletTable.put(wallet.getWalletType(), wallet.getCurreny(), wallet);
                    walletTable.notifyAll();
                }
            } catch (APIException e) {
                e.printStackTrace();
            }
        }));
	}

	/**
	 * Get all wallets
	 * @return
	 * @throws APIException
	 */
	public Collection<Wallet> getWallets() throws APIException {

		throwExceptionIfUnauthenticated();

		synchronized (walletTable) {
			return Collections.unmodifiableCollection(walletTable.values());
		}
	}

	/**
	 * Get all wallets
	 * @return
	 * @throws APIException
	 */
	public Table<String, String, Wallet> getWalletTable() throws APIException {
		return walletTable;
	}

	/**
	 * Throw a new exception if called on a unauthenticated connection
	 * @throws APIException
	 */
	private void throwExceptionIfUnauthenticated() throws APIException {
		if(! bitfinexApiBroker.isAuthenticated()) {
			throw new APIException("Unable to perform operation on an unauthenticated connection");
		}
	}

	/**
	 * Calculate the wallet margin balance for the given currency (e.g., BTC)
	 *
	 * @param symbol
	 * @throws APIException
	 */
	public void calculateWalletMarginBalance(final String symbol) throws APIException {
		throwExceptionIfUnauthenticated();

		bitfinexApiBroker.sendCommand(new CalculateCommand("wallet_margin_" + symbol));
	}

	/**
	 * Calculate the wallet funding balance for the given currency (e.g., BTC)
	 *
	 * @param symbol
	 * @throws APIException
	 */
	public void calculateWalletFundingBalance(final String symbol) throws APIException {
		throwExceptionIfUnauthenticated();

		bitfinexApiBroker.sendCommand(new CalculateCommand("wallet_funding_" + symbol));
	}

}
