package com.rad.server.access.services;

import com.rad.server.access.componenets.KeycloakAdminProperties;
import com.rad.server.access.entities.Role;
import com.rad.server.access.entities.User;
import org.apache.commons.codec.binary.Base64;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.OnClose;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Stream;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private KeycloakAdminProperties prop;

    private Keycloak getKeycloakInstance(){
        return Keycloak.getInstance(

                prop.getServerUrl(),// keycloak address
                prop.getRelm(), // ​​specify Realm master
                prop.getUsername(), // ​​administrator account
                prop.getPassword(), // ​​administrator password
                prop.getCliendId());
    }

    @Override
    public List<User> getKeycloakUsers() {
        List<User> output = new LinkedList<>();
        Keycloak keycloak = getKeycloakInstance();
        RealmResource realmResource = keycloak.realm("Admin");
        UsersResource users =  realmResource.users();
        users.list().forEach(user->output.add(new User(user.getFirstName(),user.getLastName(),user.getEmail(),user.getUsername())));
        userNameFromToken();
        return output;
    }

    @Override
    public void deleteKeycloakUser(String userName,String tenant){
        Keycloak keycloak=getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(tenant);
        UsersResource users =  realmResource.users();
        users.delete(users.search(userName).get(0).getId());
    }

    @Override
    public void addKeycloakUser(User user,ArrayList<String> tenants,String role) {
        Keycloak keycloak = getKeycloakInstance();
        String password="123";
        if(role.equals("User"))
            password="u12";
        if(role.equals("Admin"))
            password="admin";
        if(role.equals("Region-Admin"))
            password="a12";

        for (String tenant:tenants) {
            List<CredentialRepresentation> credentials=new ArrayList<>();
            UserRepresentation userRep= new UserRepresentation();
            userRep.setEnabled(true);
            userRep.setUsername(user.getUserName());
            userRep.setFirstName(user.getFirstName());
            userRep.setLastName(user.getLastName());
            userRep.setEmail(user.getEmail());
            userRep.setAttributes(Collections.singletonMap("origin", Arrays.asList("demo")));
            CredentialRepresentation credentialRepresentation=new CredentialRepresentation();
            credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
            credentialRepresentation.setValue(password);
            credentialRepresentation.setTemporary(false);
            credentials.add(credentialRepresentation);
            userRep.setCredentials(credentials);
            RealmResource realmResource = keycloak.realm(tenant);
            UsersResource usersResource = realmResource.users();
            Response response=usersResource.create(userRep);
            UserRepresentation addUserRole=usersResource.search(user.getUserName()).get(0);
            UserResource updateUser=usersResource.get(addUserRole.getId());
            List<RoleRepresentation> roleRepresentationList = updateUser.roles().realmLevel().listAvailable();

            for (RoleRepresentation roleRepresentation : roleRepresentationList)
            {
                if (roleRepresentation.getName().equals(role))
                {
                    updateUser.roles().realmLevel().add(Arrays.asList(roleRepresentation));
                    break;
                }
            }
            System.out.printf("Repsonse: %s %s%n", response.getStatus(), response.getStatusInfo());
        }
    }

    public void updateKeycloakUser(User user ,String userName){
        Keycloak keycloak=getKeycloakInstance();
        RealmResource realmResource = keycloak.realm("Admin");
        UsersResource users =  realmResource.users();
        UserRepresentation userRep=users.search(userName).get(0);
        userRep.setEmail(user.getEmail());
        userRep.setFirstName(user.getFirstName());
        userRep.setLastName(user.getLastName());
        UserResource updateUser=users.get(userRep.getId());
        updateUser.update(userRep);
    }

    public void userNameFromToken(){
        Keycloak keycloak= Keycloak.getInstance(prop.getServerUrl(),"Admin","admin","admin","customer-app","8f937a60-c546-4157-a93d-21c2b84231ee");
        String jwtToken = keycloak.tokenManager().getAccessTokenString();
        System.out.println("------------ Decode JWT ------------");
        String[] split_string = jwtToken.split("\\.");
        String base64EncodedHeader = split_string[0];
        String base64EncodedBody = split_string[1];
        String base64EncodedSignature = split_string[2];

        System.out.println("~~~~~~~~~ JWT Header ~~~~~~~");
        org.apache.commons.codec.binary.Base64 base64Url = new Base64(true);
        String header = new String(base64Url.decode(base64EncodedHeader));
        System.out.println("JWT Header : " + header);


        System.out.println("~~~~~~~~~ JWT Body ~~~~~~~");
        String body = new String(base64Url.decode(base64EncodedBody));
        System.out.println("JWT Body : "+body);

        System.out.println("ID token:"+keycloak.tokenManager().getAccessToken().getIdToken());

    }

}