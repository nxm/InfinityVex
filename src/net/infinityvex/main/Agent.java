package net.infinityvex.main;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.ConstPool;

public class Agent
{
	public static void premain(String agentArgs, Instrumentation inst) throws Exception
	{		
		final CtClass stringClazz = ClassPool.getDefault().get("java.lang.String");
		
		inst.addTransformer(new ClassFileTransformer()
		{

			@Override
			public byte[] transform(ClassLoader classLoader, String s, Class<?> aClass, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException
			{
				try
				{
					if(s != null && !s.contains("/"))
					{
						ClassPool cp = ClassPool.getDefault();
						
						CtClass cc = cp.get(s);
						
	//				    public void sendChatMessage(String message)
//					    {
					      //  this.sendQueue.addToSendQueue(new C01PacketChatMessage(message));
					    //}
					    
						if(containsString(cc, "Invalid statistic/achievement!"))
						{
							System.out.println("GuiScreen -> "+cc.getName());
							System.out.println("Searching sendChatMessage(str, bool)");
							for(CtMethod method : cc.getDeclaredMethods())
							{
								if(Modifier.isPublic(method.getModifiers()) && method.getReturnType() == CtClass.voidType && method.getParameterTypes().length == 2 && method.getParameterTypes()[0] == stringClazz && method.getParameterTypes()[1] == CtClass.booleanType)
								{
									System.out.println("Found! Hooking commandHandler...");
									method.insertBefore("{if(!net.infinityvex.main.Config.onCommand($1)){return;}}");
									break;
								}
							}
						}else
						if(containsString(cc, "abilities") && containsString(cc, "flySpeed") && containsString(cc, "mayfly"))
						{
							System.out.println("PlayerCapabilities -> "+cc.getName());
							Config.capabilitiesClass = cc.getName();
							Config.capabilitiesField = Agent.getFieldByClassWithExtends(Config.playerClass, Config.capabilitiesClass);
						}else
						if(Config.controllerClass == null && containsString(cc, "Float 4.5") && containsString(cc, "Float 5.0") && containsString(cc, "Float -180.0") && containsString(cc, "Float 10.0"))
						{
							System.out.println("Maybe PlayerController -> "+cc.getName());		
							System.out.println("Searching getReach()");							
							for(CtMethod method : cc.getDeclaredMethods())
							{
								if(Modifier.isPublic(method.getModifiers()) && method.getReturnType() == CtClass.floatType)
								{
									System.out.println("Found getReach() "+method.getName());
									Config.controllerClass = cc.getName();
									method.insertBefore("{return net.infinityvex.main.Config.getReach();}");
									break;
								}
							}
						}else
						if(containsString(cc, "minecraft:container") && containsString(cc, "minecraft:hopper"))
						{
							Config.playerClass = cc.getName();
							System.out.println("Player -> "+cc.getName());									
						}else
						if(containsString(cc, "textures/misc/shadow.png")) //renderentity
						{
							System.out.println("Render -> "+cc.getName());
							for(CtMethod method : cc.getDeclaredMethods())
							{								
								if(Modifier.isProtected(method.getModifiers()) && method.getParameterTypes().length == 6 && method.getParameterTypes()[1] == stringClazz)
								{
									method.insertAt(method.getMethodInfo().getLineNumber(0)+30, "{$2 = net.infinityvex.main.Config.onNametag($1, $2);}");
									method.insertAt(method.getMethodInfo().getLineNumber(0)+30, "{org.lwjgl.opengl.GL11.glScalef(2f,2f,2f);}");
									method.insertAt(method.getMethodInfo().getLineNumber(0)+31, "{org.lwjgl.opengl.GL11.glTranslatef(0f,-20f,0f);}");
									method.insertAt(method.getMethodInfo().getLineNumber(0)+32, "{org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);org.lwjgl.opengl.GL11.glPopMatrix();return;}");
									System.out.println("Inserting nametag hack");
//									method.insertBefore("{;}");
//								    protected void renderLivingLabel(T entityIn, String str, double x, double y, double z, int maxDistance)
									//break;
								}
							}
						}else
						if(containsString(cc, "entityBaseTick")) //entity
						{
							Config.entityClass = cc.getName();
							System.out.println("Entity -> "+Config.entityClass);							
						}else
						if(containsString(cc, "Coordinates of biome request")) //world
						{
							Config.worldClass = cc.getName();
							System.out.println("World -> "+Config.worldClass);
						
						}else
						if(containsString(cc, "textures/misc/pumpkinblur.png")) //guiingame
						{
							Config.guiIngameClass = cc.getName();
							System.out.println("GuiIngame -> "+Config.guiIngameClass);					
							
							cc.getDeclaredConstructors()[0].insertAfter("{net.infinityvex.main.Config.post();}");

							for(CtMethod method : cc.getDeclaredMethods())
							{
								if(method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == CtClass.floatType)
								{
									System.out.println("renderGameOverlay(): " + method.getName());
									method.insertAfter("{(net.infinityvex.main.Config).renderGuiHook();}");
									break;
								}else if(method.getParameterTypes().length == 4 && method.getParameterTypes()[0] == CtClass.floatType && method.getParameterTypes()[1] == CtClass.booleanType && method.getParameterTypes()[2] == CtClass.intType && method.getParameterTypes()[3] == CtClass.intType)
								{
									System.out.println("renderGameOverlay(): " + method.getName());
									method.insertAfter("{(net.infinityvex.main.Config).renderGuiHook();}");
									break;
								}
							}
										
						}
						else if(containsString(cc, "font/glyph_sizes.bin")) //fontrenderer
						{
							Config.fontRendererClass = cc.getName();
							System.out.println("FontRenderer -> "+Config.fontRendererClass);

							//Ljava/lang/String;FFIZ
							
							for(CtMethod method : cc.getDeclaredMethods())
							{
								if(method.getParameterTypes().length == 4 && method.getParameterTypes()[0] == stringClazz && method.getParameterTypes()[1] == CtClass.floatType && method.getParameterTypes()[2] == CtClass.floatType && method.getParameterTypes()[3] == CtClass.intType)
								{
									System.out.println("drawStringWithShadow(): " + method.getName());
									Config.drawStringMethod = method.getName();
									break;
								}else
								if(method.getParameterTypes().length == 4 && method.getParameterTypes()[0] == stringClazz && method.getParameterTypes()[1] == CtClass.intType && method.getParameterTypes()[2] == CtClass.intType && method.getParameterTypes()[3] == CtClass.intType)
								{
									System.out.println("drawStringWithShadow(): " + method.getName());
									Config.drawStringMethod = method.getName();
									Config.oldString = true;
									break;
								}
							}					
						}
						else if(containsString(cc, "textures/gui/title/mojang.png"))
						{				    
							Config.minecraftClass = cc.getName();
							System.out.println("Minecraft -> "+Config.minecraftClass);
							for(CtField f : cc.getDeclaredFields())
							{
								if(f.getType().getName().equals(Config.fontRendererClass))
								{
									Config.fontRendererField = f.getName();
									System.out.println("fontRenderer = "+Config.fontRendererField);
									break;
								}else
								if(f.getType().getName().equals(Config.worldClass))
								{
									Config.worldField = f.getName();
									System.out.println("worldObj = "+Config.worldField);
									break;
								}
							}
							for(CtMethod m : cc.getDeclaredMethods())
							{
								if(Modifier.isStatic(m.getModifiers()) && m.getReturnType() == cc)
								{
									Config.getMinecraftMethod = m.getName();
									System.out.println("getMinecraft(): "+Config.getMinecraftMethod);
								}
							}						
						}
						
						if(Config.playerClass == null)
						{
							if(containsString(cc, "portal.trigger"))
							{
								Config.playerClass = cc.getName();
								System.out.println("Player (1.7) -> "+cc.getName());																	
							}
						}
	
						byte[] byteCode = cc.toBytecode();
						cc.detach();
						return byteCode;		
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					System.exit(1);
				}
				return bytes;
			}

			

			private boolean containsString(CtClass cc, String string)
			{
				try
				{
					ConstPool pool = cc.getClassFile().getConstPool();
					StringWriter out = new StringWriter();
				    PrintWriter writer = new PrintWriter(out);
					pool.print(writer);					
					return out.toString().contains(string);
				}catch(Exception e)
				{
					return false;
				}
			}
		});
	}

	public static String getFieldByClass(String fromClass, String ofClass) throws NotFoundException
	{
		for(CtField f : ClassPool.getDefault().get(fromClass).getDeclaredFields())
		{
			if(f.getType().subclassOf(ClassPool.getDefault().get(ofClass)))
			{
				System.out.println("Declared field of "+ofClass+" in "+fromClass+": "+f.getName());
				return f.getName();
			}
		}
		return null;
	}
	
	public static String getFieldByClassWithExtends(String fromClass, String ofClass) throws NotFoundException
	{
		for(CtField f : ClassPool.getDefault().get(fromClass).getFields())
		{
			if(f.getType().subclassOf(ClassPool.getDefault().get(ofClass)))
			{
				System.out.println("Field of "+ofClass+" in "+fromClass+": "+f.getName());
				return f.getName();
			}
		}
		return null;
	}
}