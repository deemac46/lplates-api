//package com.dmc.lplates.inbound.controllers;
//
//import java.util.Map;
//import java.util.Optional;
//
//import com.dmc.lplates.inbound.models.Role;
//import com.dmc.lplates.inbound.service.RoleRepository;
//import jakarta.validation.Valid;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PatchMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//
//
//@RestController
//@RequestMapping("/roles")
//public class RolesController
//{
//	private final Logger log = LoggerFactory.getLogger(this.getClass());
//
//	@Autowired
//	RoleRepository roleRepository;
//
//	//REST Endpoints
//
//	@GetMapping(value = {"", "/"})
//	public Iterable<Role> index(@RequestParam("name") Optional<String> rolename)
//	{
//		log.debug("Accediendo a index() con name = {}", rolename);
//		return roleRepository.findByNameContaining(rolename.orElse(""));
//	}
//
//	@GetMapping("/{id}")
//	public ResponseEntity<Role> get(@PathVariable(value = "id") Long roleId)
//	{
//		log.debug("Accediendo a get() con id = {}", roleId);
//	    Role role = roleRepository.findById(roleId).orElse(null);
//	    if(role == null)
//	    {
//	        return ResponseEntity.notFound().build();
//	    }
//	    return ResponseEntity.ok().body(role);
//	}
//
//	@PostMapping(value = {"", "/"})
//	public Role create(@Valid @RequestBody Role role)
//	{
//		log.debug("Accediendo a create() con Role = {}", role.toString());
//	    return roleRepository.save(role);
//	}
//
//	@PutMapping("/{id}")
//	public ResponseEntity<Role> update(@PathVariable(value = "id") Long roleId,
//	                                       @Valid @RequestBody Role roleDetails) {
//
//		log.debug("Accediendo a update() con Role = {}", roleDetails.toString());
//		Role role = roleRepository.findById(roleId).orElse(null);
//	    if(role == null)
//	    {
//	        return ResponseEntity.notFound().build();
//	    }
//	    role.setName(roleDetails.getName());
//
//	    roleRepository.save(role);
//	    return ResponseEntity.ok(role);
//	}
//
//	@PatchMapping("/{id}")
//	public ResponseEntity<Role> patch(@PathVariable(value = "id") Long roleId,
//	                                       @RequestBody Map<String, String> changes) {
//
//		log.debug("Accediendo a update() con deltas = {}", changes.toString());
//		Role role = roleRepository.findById(roleId).orElse(null);
//	    if(role == null)
//	    {
//	        return ResponseEntity.notFound().build();
//	    }
//	    if(changes.containsKey("name")) role.setName(changes.get("name"));
//
//	    roleRepository.save(role);
//	    return ResponseEntity.ok(role);
//	}
//
//
//
//
//}
