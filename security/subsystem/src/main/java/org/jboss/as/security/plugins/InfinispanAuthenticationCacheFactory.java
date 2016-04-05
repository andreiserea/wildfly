/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.as.security.plugins;

import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.security.authentication.JBossCachedAuthenticationManager.DomainInfo;

import java.security.Principal;
import java.util.concurrent.ConcurrentMap;

/**
 * Factory that creates ISPN {@code ConcurrentMap}s for authentication cache.
 *
 * @author Eduardo Martins
 */
public class InfinispanAuthenticationCacheFactory implements AuthenticationCacheFactory {

    private final EmbeddedCacheManager cacheManager;
    private final String securityDomain;

    /**
     *
     * @param cacheManager
     * @param securityDomain
     */
    public InfinispanAuthenticationCacheFactory(Object cacheManager, String securityDomain) {
        this.cacheManager = (EmbeddedCacheManager) cacheManager;
        this.securityDomain = securityDomain;
    }

    /**
     * Returns a default cache implementation
     *
     * @return cache implementation
     */
    public ConcurrentMap<Principal, DomainInfo> getCache() {
        // TODO override global settings with security domain specific
        ConfigurationBuilder builder = new ConfigurationBuilder();
        // hack: if auth-cache configuration should be present wait until it is loaded
        // see https://issues.jboss.org/browse/WFLY-3858
        String prop = System.getProperty("com.capgemini.ibx.auth-cache.defined");
        Configuration baseCfg;
        do {
            baseCfg = cacheManager.getCacheConfiguration("auth-cache");
            if (baseCfg != null) {
                builder.read(baseCfg);
            } else {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } while (baseCfg == null && prop != null);

        cacheManager.defineConfiguration(securityDomain, builder.build());
        return cacheManager.getCache(securityDomain);
    }
}
