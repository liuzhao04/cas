package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.Principal;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;

import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link PrincipalAttributeRepositoryFetcher}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Builder
@Getter
@Slf4j
public class PrincipalAttributeRepositoryFetcher {
    private final IPersonAttributeDao attributeRepository;

    @Nonnull
    private final String principalId;

    @Builder.Default
    private final Set<String> activeAttributeRepositoryIdentifiers = new HashSet<>();

    private final Principal currentPrincipal;

    /**
     * Retrieve person attributes.
     *
     * @return the map
     */
    public Map<String, List<Object>> retrieve() {
        var filter = IPersonAttributeDaoFilter.alwaysChoose();
        if (!activeAttributeRepositoryIdentifiers.isEmpty()) {
            val repoIdsArray = activeAttributeRepositoryIdentifiers.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
            filter = dao -> Arrays.stream(dao.getId())
                .anyMatch(daoId -> daoId.equalsIgnoreCase(IPersonAttributeDao.WILDCARD)
                    || StringUtils.equalsAnyIgnoreCase(daoId, repoIdsArray)
                    || StringUtils.equalsAnyIgnoreCase(IPersonAttributeDao.WILDCARD, repoIdsArray));
        }

        val queryAttributes = new HashMap<String, Object>();
        queryAttributes.put("username", principalId);

        if (currentPrincipal != null) {
            queryAttributes.put("principal", currentPrincipal.getId());
            queryAttributes.putAll(currentPrincipal.getAttributes());
        }

        LOGGER.trace("Fetching person attributes for query [{}]", queryAttributes);
        val people = attributeRepository.getPeople(queryAttributes, filter);
        if (people == null || people.isEmpty()) {
            LOGGER.warn("No person records were fetched from attribute repositories for [{}]", queryAttributes);
            return new HashMap<>(0);
        }

        if (people.size() > 1) {
            LOGGER.warn("Multiple records were found for [{}] from attribute repositories for query [{}]. The records are [{}], "
                    + "and CAS will only pick the first person record from the results.",
                principalId, queryAttributes, people);
        }

        val person = people.iterator().next();
        LOGGER.debug("Retrieved person [{}] from attribute repositories for query [{}]", person, queryAttributes);
        return person.getAttributes();
    }
}
