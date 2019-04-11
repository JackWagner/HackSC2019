//
//  Simulator.swift
//  HackSC
//
//  Created by User on 4/10/19.
//  Copyright © 2019 Rohan Krishnaswamy. All rights reserved.
//

import Foundation

import SpriteKit
import GameplayKit

class Simulator: SKScene
{
    let track: SKShapeNode!
    let trackRadius: CGFloat!
    var currentParticle: SKShapeNode?
    var vehicles: [(vehicle: SKShapeNode, speed: CGFloat)]!
    
    override init(size: CGSize)
    {
        self.trackRadius = 170.0
        self.track = SKShapeNode(circleOfRadius: trackRadius)
        self.currentParticle = nil
        self.vehicles = [(vehicle: SKShapeNode, speed: CGFloat)]()
        
        super.init(size: size)
    }
    
    required init?(coder aDecoder: NSCoder)
    {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func didMove(to view: SKView)
    {
        //Set background color
        self.backgroundColor = UIColor.white
        
        //Create our track, objects will move around it
        self.track.position = CGPoint(x: frame.midX, y: frame.midY)
        self.track.fillColor = UIColor.lightGray
        self.track.strokeColor = UIColor.black
        self.track.glowWidth = 1.0
        
        self.addChild(self.track)
        
        //createParticle()
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?)
    {
        let touch: UITouch = touches.first!
        let positionInScene = touch.location(in: self)
        let touchedNode = self.atPoint(positionInScene)
        
        if let name = touchedNode.name
        {
            if name == "vehicle"
            {
                for v in self.vehicles
                {
                    //Reset attributes for non-selected vehicle
                    v.vehicle.fillColor = UIColor.red
                    v.vehicle.strokeColor = UIColor.blue
                }
                
                //touchedNode.removeFromParent()
                
                //Give current Particle different look
                self.currentParticle = (touchedNode as! SKShapeNode)
                self.currentParticle!.fillColor = UIColor.blue
                self.currentParticle!.strokeColor = UIColor.orange
                
                for v in self.vehicles
                {
                    if v.vehicle == touchedNode
                    {
                        let new_Speed: CGFloat = 100.0
                        
                        //v.vehicle.editActionSpeed(actionKey: "Track_Path", currentSpeed: v.speed, newSpeed: new_Speed)
                        
                        //v.vehicle.physicsBody?.applyImpulse(CGVector(dx: 10.0, dy: 10.0))
                    }
                    else
                    {
                        //Restore speed of other vehicles?
                        //We can play around with this
                        
                        let new_Speed: CGFloat = 150.0
                        
                        //v.vehicle.editActionSpeed(actionKey: "Track_Path", currentSpeed: v.speed, newSpeed: new_Speed)
                    }
                }
                
            }
        }
        else
        {
            createParticle()
        }
    }
    
    func createParticle()
    {
        //Create a new particle
        let particle = SKShapeNode(rectOf: CGSize(width: 10.0, height: 10.0))
        particle.position = CGPoint(x: frame.midX, y: frame.midY)
        particle.name = "vehicle"
        particle.fillColor = UIColor.red
        particle.strokeColor = UIColor.blue
        particle.glowWidth = 1.0
        
        //Add a physics body to the particle
        particle.physicsBody = SKPhysicsBody(rectangleOf: CGSize(width: 10, height: 10))
        particle.physicsBody?.affectedByGravity = false
        
        //Add particle to parent (track)
        self.track.addChild(particle)
        
        let particle_Speed: CGFloat = 150.0
        
        self.vehicles.append((vehicle: particle, speed: particle_Speed))
        
        //Create an action for the particle to follow
        //Repeat forever
        let pathAction = SKAction.repeatForever(SKAction.follow(self.track.path!, asOffset: false, orientToPath: true, speed: particle_Speed))
        
        particle.run(pathAction, withKey: "Track_Path")
    }
    
    func getGapBetween(particle1: SKShapeNode, particle2: SKShapeNode) -> CGFloat
    {
        let a1 = acos(particle1.position.x / self.trackRadius)
        let a2 = acos(particle2.position.x / self.trackRadius)
        
        //return CGFloat(self.trackRadius! * (((particle1.position.y < 0) ? (2 * CGFloat((Double.pi)) - a1) : a1) - ((particle2.position.y < 0) ? (2 * CGFloat((Double.pi)) - a2) : a2)))
        
        
        return (self.trackRadius * acos(dotProd(particle1: particle1, particle2: particle2) /
                (magnitude(particle: particle1) * magnitude(particle: particle2))))
        
        
        
        
        //return CGFloat(self.trackRadius * (abs(asin(particle1.position.x / self.trackRadius)))) //- abs(asin(particle2.position.x / self.trackRadius))))
        
        //return CGFloat(sqrtf(Float(pow((particle2.position.x - particle1.position.x), 2) +
           // pow((particle2.position.y - particle1.position.y), 2))))
    }
    
    func dotProd(particle1: SKShapeNode, particle2: SKShapeNode) -> CGFloat
    {
        return (particle1.position.x * particle2.position.x) + (particle1.position.y * particle2.position.y)
    }
    
    func magnitude(particle: SKShapeNode) -> CGFloat
    {
        return CGFloat(sqrt(pow(particle.position.x, 2) + pow(particle.position.y, 2)))
    }
    
    
    override func update(_ currentTime: TimeInterval)
    {
        super.update(currentTime)
        
        if self.vehicles.count == 3
        {
            print(getGapBetween(particle1: self.vehicles[2].vehicle, particle2: self.vehicles[0].vehicle))
        }
    }
}

extension SKNode
{
    func editActionSpeed(actionKey: String, currentSpeed: CGFloat, newSpeed: CGFloat)
    {
        self.action(forKey: actionKey)?.speed = newSpeed / currentSpeed
        
        //
    }
}
