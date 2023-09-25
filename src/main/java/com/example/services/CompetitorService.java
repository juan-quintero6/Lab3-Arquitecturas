/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.services;

import com.example.PersistenceManager;
import com.example.models.Competitor;
import com.example.models.CompetitorDTO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Mauricio
 */
@Path("/competitors")
@Produces(MediaType.APPLICATION_JSON)
public class CompetitorService {

    @PersistenceContext(unitName = "CompetitorsPU")
    EntityManager entityManager;
    
    @PostConstruct
    
    public void init() {
        try {
            entityManager =
        PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        } catch (Exception e) {
        e.printStackTrace();
        }
    }
    
    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {

        Query q = entityManager.createQuery("select u from Competitor u order by u.surname ASC");
        
        List<Competitor> competitors = q.getResultList();
        //Competitor competitorTmp= new Competitor("Carlos", "Alvarez", 35, "7658463", "3206574839 ", "carlos.alvarez@gmail.com", "Bogota", "Colombia", false);
        //Competitor competitorTmp2= new Competitor("Gustavo", "Ruiz", 55, "2435231", "3101325467", "gustavo.ruiz@gmail.com", "Buenos Aires", "Argentina", false);
        //competitors.add(competitorTmp);
        //competitors.add(competitorTmp2);
        return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(competitors).build();
    }

    @POST
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCompetitor(CompetitorDTO competitor) {
        JSONObject rta = new JSONObject();
        //Competitor competitorTmp= new Competitor(competitor.getName(), competitor.getSurname(), competitor.getAge(), competitor.getTelephone(), competitor.getCellphone(), competitor.getAddress(), competitor.getCity(), competitor.getCountry(), false);
        Competitor competitorTmp = new Competitor();
        competitorTmp.setAddress(competitor.getAddress());
        competitorTmp.setPassword(competitor.getPassword());
        competitorTmp.setAge(competitor.getAge());
        competitorTmp.setCellphone(competitor.getCellphone());
        competitorTmp.setCity(competitor.getCity());
        competitorTmp.setCountry(competitor.getCountry());
        competitorTmp.setName(competitor.getName());
        competitorTmp.setSurname(competitor.getSurname());
        competitorTmp.setTelephone(competitor.getTelephone());
        
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(competitorTmp);
            entityManager.getTransaction().commit();
            entityManager.refresh(competitorTmp);
            rta.put("competitor_id", competitorTmp.getId());
        } catch (Throwable t) {
            t.printStackTrace();
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            competitorTmp = null;
            } finally {
            entityManager.clear();
            entityManager.close();
        }

        return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(competitorTmp).build();
    }
    
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response autenticar(Competitor competitorTmp) {
        try {
            // Crea una consulta personalizada para buscar un usuario por correo y contraseña
            String address = competitorTmp.getAddress();
            String password = competitorTmp.getPassword();
            
            Query query = entityManager.createQuery("SELECT u FROM Competitor u WHERE u.address = :address AND u.password = :password");
            query.setParameter("address", address);
            query.setParameter("password", password);

            // Intenta obtener un resultado único
            Competitor competitorCheck = (Competitor) query.getSingleResult();

            // Si se encontró un usuario, devuelve una respuesta exitosa
            return Response.status(Response.Status.OK).entity(competitorCheck).build();
        } catch (NoResultException e) {
            // Si no se encuentra un resultado, devuelve una respuesta de error
            return Response.status(Response.Status.UNAUTHORIZED).entity("Credenciales inválidas").build();
        }
    }    

}
