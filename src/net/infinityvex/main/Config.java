package net.infinityvex.main;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import javassist.ClassPool;
import javassist.CtField;

public class Config
{
	public static String main = "";
	public static Object minecraft = null;
	protected static String fontRendererClass;
	protected static String fontRendererField, capabilitiesField;
	protected static String drawStringMethod;
	protected static String minecraftClass;
	protected static String guiIngameClass, controllerClass, capabilitiesClass;
	protected static String getMinecraftMethod;
	protected static boolean oldString, flying;
	protected static String worldClass;
	protected static String entitiesField;
	protected static String worldField;
	protected static String entityClass;
	protected static String playerField;
	protected static String playerClass;
	
	public static void renderGuiHook()
	{
		try
		{
			Method m = Class.forName(minecraftClass).getMethod(getMinecraftMethod);
			minecraft = m.invoke(null);
			
			Object world = minecraft.getClass().getField(Config.worldField).get(minecraft);
			List<?> entities = (List)world.getClass().getField(entitiesField).get(world);
						
			drawString("§9Infinity§e§lVex", 3f, 3f, 0xffffff);
			drawString("§7entities§8: §6"+entities.size(), 6f, 13f, 0xffffff);
			drawString("§7flying§8: "+format(flying), 6f, 23f, 0xffffff);
			
			if(flying)
				setFly(true);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static String format(boolean bool)
	{
		return bool ? "§atrue" : "§cfalse";
	}

	public static String onNametag(Object entity, String name)
	{
	    //public final float getHealth()
				
		float max = -1;
		float hp = -1;

		try
		{			
			for(Method m : entity.getClass().getMethods())
			{
				if(m.getReturnType() == float.class && Modifier.isPublic(m.getModifiers()) && Modifier.isFinal(m.getModifiers()) && m.getParameters().length == 0)
				{
					if(max == -1)
						max = (float)m.invoke(entity);
					else
						hp = (float)m.invoke(entity);
					
					if(max != -1 && hp != -1)
						break;
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		drawString("§6"+name, -10, -15, 0xffffff);
		drawString("§c"+hp+"/"+max, -20, -5, 0xffffff);
		return name;
	}
	
	public static void post()
	{
		try
		{
			System.out.println("Game loaded");
			
			Config.fontRendererField = Agent.getFieldByClass(Config.minecraftClass, Config.fontRendererClass);
			Config.worldField = Agent.getFieldByClass(Config.minecraftClass, Config.worldClass);
			Config.playerField = Agent.getFieldByClass(Config.minecraftClass, Config.playerClass);
			
			for(CtField f : ClassPool.getDefault().get(worldClass).getFields())
			{
				if(f.getType().equals(ClassPool.getDefault().get("java.util.List")))
				{
					System.out.println("Entity list: "+f.getName());
					Config.entitiesField = f.getName();
					break;
				}
			}			
		}catch(Exception e)
		{
			e.printStackTrace();			
		}
	}
	
	public static void drawString(String string, float x, float y, int color)
	{
		try
		{			
			Field f = minecraft.getClass().getField(fontRendererField);
			Object fontRenderer = f.get(minecraft);

			if(oldString)
			{
				Method draw = fontRenderer.getClass().getMethod(drawStringMethod, String.class, int.class, int.class, int.class);		
				draw.invoke(fontRenderer, string, (int)x, (int)y, color);
			}else
			{
				Method draw = fontRenderer.getClass().getMethod(drawStringMethod, String.class, float.class, float.class, int.class);		
				draw.invoke(fontRenderer, string, x, y, color);
			}
		}catch(Exception e)
		{
			e.printStackTrace();			
		}
	}
	
	public static float getReach()
	{
		return 100f;
	}
	
	public static boolean onCommand(String msg)
	{
		try
		{
			if(msg.startsWith("."))
			{
				if(msg.equals(".fly"))
				{
					flying = !flying;
					setFly(flying);
				}
				return false;
			}
			return true;
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	private static void setFly(boolean flying) throws Exception
	{
		Object player = minecraft.getClass().getField(playerField).get(minecraft);
		Object capabilities = player.getClass().getField(capabilitiesField).get(player);
		
		int aa = 0;
		for(Field f : capabilities.getClass().getFields())
		{
			if(f.getType() == boolean.class)
			{
				if(aa == 1 || aa == 2)
				{
					f.set(capabilities, flying);
				}
				aa++;
			}
		}		
	}
}
