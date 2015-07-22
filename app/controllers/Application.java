package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Authtoken;
import models.Client;
import models.ResultStatus;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


    public class Application extends Controller {
        public String clientId = null;
        public String clientSecret = null;

        /**
         * This method is to verify if the client is authenticated.
         *
         * @return Result Returns authenticated/unauthenticated status for the client
         */
        @Transactional
        @BodyParser.Of(BodyParser.Json.class)
        public Result verifyClient() {
            ResultStatus resultStatus=null;
            JsonNode resultJson = null;
            int expiryHours = 24;
            EntityManager em = JPA.em();
            try {
                String authToken = null;
                RequestBody requestBody = request().body();
                clientId = requestBody.asJson().get("clientid").asText();
                clientSecret = requestBody.asJson().get("clientsecret").asText();
                Query query = em.createQuery("Select c from Client c where c.clientId=:clientId and c.clientSecret=:clientSecret");
                query.setParameter("clientId", clientId);
                query.setParameter("clientSecret", clientSecret);
                Client client = (Client) query.getSingleResult();
                System.out.println(client.getClientId() + " " + client.getClientSecret());
                UUID uuid = UUID.randomUUID();
                authToken = uuid.toString();
                resultStatus = new ResultStatus();
                resultStatus.setSuccess("true");
                resultStatus.setResult(authToken);
                resultJson = Json.toJson(resultStatus);
                updateDBWithToken(clientId, authToken, expiryHours);
                return ok(resultJson);
            } catch (NoResultException e) {
                resultStatus = new ResultStatus();
                resultStatus.setSuccess("false");
                resultStatus.setResult("Unauthenticated");
                resultJson = Json.toJson(resultStatus);
                return unauthorized(resultJson);
            }
            catch(Exception exp)
            {
                return badRequest(exp.getMessage());
            }

        }
            /**
             * This method is used to update the DB with client's token
             */

            public void updateDBWithToken(String clientId, String authToken, int expiryHours) {
                String dateToInsert=null;

                try {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date(new Date().getTime() + (1000 * 60 * 60 * expiryHours));
                    dateToInsert = dateFormat.format(date);
                    Query query = JPA.em().createQuery("select a from Authtoken a where a.clientId=:clientId");
                    query.setParameter("clientId", clientId);
                    Authtoken authTokenObject = (Authtoken) query.getSingleResult();
                    updateIntoDB(authTokenObject.getClientId(),authToken,dateToInsert);

                } catch (NoResultException e) {
                    insertIntoDB(clientId,authToken,dateToInsert);
                }
                catch(Exception exp)
                {
                   Logger.info(exp.getMessage());
                }
            }

         /**
             * This method is used for inserting client details into authtoken table.
         */
       public void insertIntoDB(String clientId, String authToken, String dateToInsert) {
           EntityManager em = JPA.em();
           Authtoken token=new Authtoken();
           token.setClientId(clientId);
           token.setToken(authToken);
           token.setExpiryDate(dateToInsert);
           em.persist(token);
           Logger.info("Inserted client info successfully!");

        }

       /**
             * This method is used for updating client details into authtoken table.
        */
       public void updateIntoDB(String clientId, String authToken, String dateToInsert) {
           EntityManager em = JPA.em();
           Logger.info("Updating...");
           Query query = em.createQuery("Select a from Authtoken a where a.clientId=:clientId");
           query.setParameter("clientId", clientId);
           Authtoken authTokenObject = (Authtoken) query.getSingleResult();
           authTokenObject.setToken(authToken);
           authTokenObject.setExpiryDate(dateToInsert);
           Logger.info("Updated client info successfully!");
        }

         /**
             * This method is used for validating the authtoken.
         */
         @Transactional
         @BodyParser.Of(BodyParser.Json.class)
         public Result validateAuthToken() {
                EntityManager em = JPA.em();
                String authToken=null;
                ResultStatus resultStatus=null;
                JsonNode resultJson = null;

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    RequestBody requestBody = request().body();
                    clientId = requestBody.asJson().get("clientid").asText();
                    authToken = request().getHeader("AuthorizationBearer");
                    Query query = em.createQuery("select a from Authtoken a where a.clientId=:clientId and a.token=:token");
                    query.setParameter("clientId", clientId);
                    query.setParameter("token",authToken);
                    Authtoken authTokenObject = (Authtoken) query.getSingleResult();
                    String expiryDate=authTokenObject.getExpiryDate();
                    Date expDate = formatter.parse(expiryDate);
                    if(expDate.compareTo(new Date())>0)
                    {
                        resultStatus=new ResultStatus();
                        resultStatus.setSuccess("true");
                        resultStatus.setResult("Authorized! Valid Token");
                        resultJson = Json.toJson(resultStatus);
                        return ok(resultJson);
                    }
                    else {
                        resultStatus=new ResultStatus();
                        resultStatus.setSuccess("false");
                        resultStatus.setResult("UnAuthorized! Token expired. Please send a request for a new token");
                        resultJson = Json.toJson(resultStatus);
                        return unauthorized(resultJson);
                    }
                }
                catch(NoResultException e)
                {
                    resultStatus=new ResultStatus();
                    resultStatus.setSuccess("false");
                    resultStatus.setResult("UnAuthorized! Invalid Token");
                    resultJson = Json.toJson(resultStatus);
                    return unauthorized(resultJson);
                } catch (ParseException e) {
                  System.out.println(e.getMessage());
                }
             catch(Exception exp)
             {
                 return badRequest(exp.getMessage());
             }
             return ok();
         }

        }

