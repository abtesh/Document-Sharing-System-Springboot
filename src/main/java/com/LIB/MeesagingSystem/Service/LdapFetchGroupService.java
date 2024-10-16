package com.LIB.MeesagingSystem.Service;


import com.LIB.MeesagingSystem.Dto.LdapGroup;
import com.LIB.MeesagingSystem.Dto.LdapUser;
import com.LIB.MeesagingSystem.Model.Enums.GroupType;
import com.LIB.MeesagingSystem.Model.Group;
import com.LIB.MeesagingSystem.Model.Users;
import com.LIB.MeesagingSystem.Repository.GroupRepository;
import com.LIB.MeesagingSystem.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.PartialResultException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LdapFetchGroupService {
    private final LdapTemplate ldapTemplate;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ReentrantLock lock = new ReentrantLock();


    @Async
    public void getAllGroupsAndMembers() {
        if (lock.tryLock()) {  // Attempt to acquire the lock
            try {
                log.info("Fetching groups and members from LDAP");
                List<LdapGroup> groups = this.getAllGroups();

                groups = groups.stream()// Filter groups with mail only
                        .peek(group -> {
                            if (group.getManagedBy() != null) {
                                group.setManagedBy(this.searchByFirstName(group.getManagedBy()).getDisplayName());
                            }
                            if (group.getMembersName() != null) {
                                group.setMembers(group.getMembersName().stream()
                                        .map(this::searchByFirstName)
                                        .collect(Collectors.toList()));
                            }
                        })
                        .collect(Collectors.toList());

                groups.stream().forEach(g -> this.groupPersistentDb(g));

                log.info("successfully fetched groups and members");
            }
            catch (Exception e) {
                log.error("error occurred while retrieving groups and members{}", e.getMessage());

            }finally {
                lock.unlock();
            }
        } else {
            log.warn("Another request is already processing, please wait until it finishes.");
            throw new IllegalStateException("The method is already running in the background, please wait until it finishes.");
        }
    }





    public void groupPersistentDb(LdapGroup ldapGroup) {
        if (ldapGroup == null || ldapGroup.getId() == null) {
            log.error("LdapGroup or its ID cannot be null");
            throw new IllegalArgumentException("LdapGroup or its ID cannot be null");
        }

        log.info("Processing group: {}", ldapGroup.getCn());

        // Initialize lists and variables
        List<Users> groupMembers = new ArrayList<>();
        Users groupManager;

        // Process group members
        List<LdapUser> ldapGroupMembers = ldapGroup.getMembers();
        if (ldapGroupMembers != null) {
            ldapGroupMembers.stream()
                    .filter(member -> member != null && member.getId() != null)
                    .forEach(member -> {
                        Users user = userRepository.findById(member.getId())
                                .map(existingUser -> updateUser(existingUser, member))
                                .orElseGet(() -> saveNewUser(member));
                        groupMembers.add(user);
                        log.info("Processed member: {} (ID: {})", user.getName(), user.getId());
                    });
        } else {
            log.warn("Group members list is null for group: {}", ldapGroup.getCn());
        }

// Process group manager
        if (ldapGroup.getManagedBy() != null && ldapGroup.getManager() != null && ldapGroup.getManager().getId() != null) {
            groupManager = userRepository.findById(ldapGroup.getManager().getId())
                    .map(existingUser -> updateUser(existingUser, ldapGroup.getManager()))
                    .orElseGet(() -> saveNewUser(ldapGroup.getManager()));
            log.info("Processed manager: {} (ID: {})", groupManager.getName(), groupManager.getId());
        } else {
            groupManager = null;
        }

        // Process group
        Group group = groupRepository.findById(ldapGroup.getId())
                .map(existingGroup -> updateGroup(existingGroup, ldapGroup, groupManager, groupMembers))
                .orElseGet(() -> createNewGroup(ldapGroup, groupManager, groupMembers));
        groupRepository.save(group);
        log.info("Group {} (ID: {}) has been successfully persisted", group.getName(), group.getId());
    }

    private Users updateUser(Users existingUser, LdapUser ldapUser) {
        log.debug("Updating user: {} (ID: {})", existingUser.getName(), existingUser.getId());
        existingUser.setName(ldapUser.getDisplayName());
        existingUser.setEmail(ldapUser.getMail());
        return userRepository.save(existingUser);
    }

    private Users saveNewUser(LdapUser ldapUser) {
        log.debug("Saving new user: {} (ID: {})", ldapUser.getDisplayName(), ldapUser.getId());
        Users newUser = Users.builder()
                .id(ldapUser.getId())
                .name(ldapUser.getDisplayName())
                .email(ldapUser.getMail())
                .isActive(true)
                .build();
        return userRepository.save(newUser);
    }

    private Group updateGroup(Group existingGroup, LdapGroup ldapGroup, Users groupManager, List<Users> groupMembers) {
        log.debug("Updating group: {} (ID: {})", existingGroup.getName(), existingGroup.getId());
        existingGroup.setName(ldapGroup.getCn());
        existingGroup.setMail(ldapGroup.getMail());
        existingGroup.setGroupType(GroupType.LDAP);
        existingGroup.setDescription(ldapGroup.getDescription());
        existingGroup.setMakerId(groupManager == null ? null : groupManager.getId());
        existingGroup.setMembers(groupMembers.stream().map(Users::getId).toList());
        return existingGroup;
    }

    private Group createNewGroup(LdapGroup ldapGroup, Users groupManager, List<Users> groupMembers) {
        log.debug("Creating new group: {} (ID: {})", ldapGroup.getCn(), ldapGroup.getId());
        return Group.builder()
                .id(ldapGroup.getId())
                .creationDate(new Date())
                .name(ldapGroup.getCn())
                .groupType(GroupType.LDAP)
                .description(ldapGroup.getDescription())
                .makerId(groupManager == null ? null : groupManager.getId())
                .members(groupMembers.stream().map(Users::getId).toList())
                .build();
    }

    public List<LdapGroup> getAllGroups() {
        // Initialize the list outside the try-catch block
        List<LdapGroup> groups = new ArrayList<>();

        try {
            String searchFilter = "(&(objectClass=group)(member=*))";

            ldapTemplate.setIgnorePartialResultException(true);  // Ignore continuation references

            // Perform the LDAP search and add results to the groups list
            groups = ldapTemplate.search("DC=LIB5,DC=COM", searchFilter, (AttributesMapper<LdapGroup>) attributes -> {
                if(attributes.get("cn").toString()==("Microsoft Exchange Security Groups") || attributes.get("cn").toString().equals("ForeignSecurityPrincipals"))
                    return LdapGroup.builder().build();
                 LdapGroup group = new LdapGroup();
                byte[] guidBytes = (byte[]) attributes.get("objectGUID").get();
                group.setId(convertBytesToGUID(guidBytes));

                // Set 'cn'
                group.setCn(attributes.get("cn") != null ? attributes.get("cn").get().toString() : null);

                // Set 'description'
                group.setDescription(attributes.get("description") != null ? attributes.get("description").get().toString() : null);

                // Set 'mail'
                group.setMail(attributes.get("mail") != null ? attributes.get("mail").get().toString() : null);

                // Set 'managedBy' and search manager details
                group.setManagedBy(attributes.get("managedBy") != null ? attributes.get("managedBy").get().toString() : null);
                group.setManager(searchByFirstNameForManagers(group.getManagedBy()));

                // Handling multiple 'member' values
                if (attributes.get("member") != null) {
                    NamingEnumeration<?> members = attributes.get("member").getAll();
                    List<String> memberList = new ArrayList<>();
                    while (members.hasMore()) {
                        memberList.add(members.next().toString());
                    }
                    group.setMembersName(memberList);
                } else {
                    group.setMembers(null);
                }

                return group;
            });
        } catch (PartialResultException e) {
            String remainingName = e.getRemainingName().toString();
            log.warn("PartialResultException: Unprocessed Continuation Reference(s): {}", e.getMessage());

            // Check if remaining name is 'DC=LIB5,DC=COM' and stop iteration if so
            if ("DC=LIB5,DC=COM".equals(remainingName)) {
                log.warn("Stopping iteration due to remaining name: {}", remainingName);
            }
            // Handle the exception but return the accumulated results so far
        } catch (Exception e) {
            log.error("An error occurred during LDAP search: {}", e.getMessage());
        }

        // Return the accumulated groups, even if an exception occurs
        return groups;
    }



    public LdapUser searchByFirstName(String dn) {
        if (dn.equals("CN=admin,OU=ITS Users,OU=IT,DC=LIB5,DC=COM"))
            return new LdapUser();
        if (dn.equals("CN=Desta G\\\\tsadikan,OU=adihki users,OU=Adihaki Branch,OU=Northern Outline Branches,OU=LIB Users & Computers,DC=LIB5,DC=COM"))
            return new LdapUser();
        if(dn.equals("CN=Exchange Servers,OU=Microsoft Exchange Security Groups,DC=LIB5,DC=COM"))
            return new LdapUser();


        try {
            return ldapTemplate.lookup(escapeDn(dn), (AttributesMapper<LdapUser>) attrs -> {
                if (isUserOrPerson(attrs)) {
                    LdapUser user = new LdapUser();
                    byte[] guidBytes = (byte[]) attrs.get("objectGUID").get();
                    user.setId(convertBytesToGUID(guidBytes));
                    user.setCn(attrs.get("cn") != null ? attrs.get("cn").get().toString() : null);
                    user.setMail(attrs.get("userPrincipalName") != null ? attrs.get("userPrincipalName").get().toString().toLowerCase() : null);
                    user.setDisplayName(user.getCn());
                    return user;
                }
                log.info(dn);
                return new LdapUser();
            });
        } catch (Exception e) {
            log.error("Error retrieving LDAP user for dn: {} - {}", dn, e.getMessage());
            return null;
        }
    }

    public LdapUser searchByFirstNameForManagers(String dn) {
        log.warn(dn + " is searching for managers");
        if (dn == null)
            return null;
        if (dn.equals("CN=admin,OU=ITS Users,OU=IT,DC=LIB5,DC=COM"))
            return null;
        if (dn.equals("CN=Desta G\\\\tsadikan,OU=adihki users,OU=Adihaki Branch,OU=Northern Outline Branches,OU=LIB Users & Computers,DC=LIB5,DC=COM"))
            return null;


        try {
            return ldapTemplate.lookup(escapeDn(dn), (AttributesMapper<LdapUser>) attrs -> {
                if (isUserOrPerson(attrs)) {
                    LdapUser user = new LdapUser();
                    byte[] guidBytes = (byte[]) attrs.get("objectGUID").get();
                    user.setId(convertBytesToGUID(guidBytes));
                    user.setCn(attrs.get("cn") != null ? attrs.get("cn").get().toString() : null);
                    user.setMail(attrs.get("userPrincipalName") != null ? attrs.get("userPrincipalName").get().toString().toLowerCase() : null);
                    user.setDisplayName(user.getCn());
                    return user;
                }
                log.warn(dn);
                return null;
            });
        } catch (Exception e) {
            // Log the exception
            System.err.println("Error retrieving LDAP user: " + e.getMessage());
            return null;
        }
    }

    private boolean isUserOrPerson(Attributes attrs) throws NamingException {
        Attribute objectClassAttr = attrs.get("objectClass");
        if (objectClassAttr != null) {
            for (int i = 0; i < objectClassAttr.size(); i++) {
                String objectClass = (String) objectClassAttr.get(i);
                if ("user".equalsIgnoreCase(objectClass) || "person".equalsIgnoreCase(objectClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String convertBytesToGUID(byte[] guidBytes) {
        UUID uuid = UUID.fromString(
                String.format("%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
                        guidBytes[3] & 255,
                        guidBytes[2] & 255,
                        guidBytes[1] & 255,
                        guidBytes[0] & 255,
                        guidBytes[5] & 255,
                        guidBytes[4] & 255,
                        guidBytes[7] & 255,
                        guidBytes[6] & 255,
                        guidBytes[8] & 255,
                        guidBytes[9] & 255,
                        guidBytes[10] & 255,
                        guidBytes[11] & 255,
                        guidBytes[12] & 255,
                        guidBytes[13] & 255,
                        guidBytes[14] & 255,
                        guidBytes[15] & 255));
        return uuid.toString();
    }

    public String escapeDn(String dn) {
        return dn
                .replace("\\", "\\\\")  // Escape backslashes
                .replace("/", "\\/")    // Escape forward slashes
                .replace("'", "\\'");   // Escape single quotes
    }

}
