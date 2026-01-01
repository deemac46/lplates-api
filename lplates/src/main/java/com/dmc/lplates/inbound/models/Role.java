//package com.dmc.lplates.inbound.models;
//
//
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import org.jetbrains.annotations.NotNull;
//
//
//@Entity
//public class Role
//{
//	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
//	private Long id;
//
//	@NotNull
//	private String name;
//
//	public Long getId()
//	{
//		return id;
//	}
//
//	public void setId(Long id)
//	{
//		this.id = id;
//	}
//
//	public String getName()
//	{
//		return name;
//	}
//
//	public void setName(String name)
//	{
//		this.name = name;
//	}
//
//	public String toString()
//	{
//		ObjectMapper objectMapper = new ObjectMapper();
//		try
//		{
//			return objectMapper.writeValueAsString(this);
//		}
//		catch(JsonProcessingException jpe)
//		{
//			throw new RuntimeException(jpe);
//		}
//	}
//
//}
