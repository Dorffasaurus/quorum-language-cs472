/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins.quorum.Libraries.Game.Graphics;


import quorum.Libraries.Game.Graphics.PixelMap_;

import plugins.quorum.Libraries.Game.GameRuntimeError;
import plugins.quorum.Libraries.Game.libGDX.Array;

import java.util.HashMap;
import java.util.Map;
import plugins.quorum.Libraries.Game.GameState;

/**
 * The Texture class stores information regarding OpenGL textures, which is
 * needed each time the Texture is drawn. The bitmap used by this texture is
 * generated by PixelMap.
 */
public class Texture
{
  public java.lang.Object me_ = null;
  
  public int glTarget; //The target, such as GL_TEXTURE_2D
  protected int glHandle;
  
  private GraphicsManager gl20 = GameState.nativeGraphics; 
                            
  TextureData data;
  
  // This array holds all textures which are managed by an application, i.e., 
  // textures that will be automatically reloaded whenever an application has
  // regained context after losing it.
  final static Map<quorum.Libraries.Game.Application_, Array<Texture>> managedTextures = new HashMap<quorum.Libraries.Game.Application_, Array<Texture>>();

   
    public void LoadFromTextureData(quorum.Libraries.Game.Graphics.TextureData_ data)
    {
        glTarget = GraphicsManager.GL_TEXTURE_2D;
        glHandle = CreateGLHandle();
    }

    public void Bind() 
    {
        gl20.glBindTexture(glTarget, glHandle);
    }
    
    public void Bind(int unit)
    {
        gl20.glActiveTexture(gl20.GL_TEXTURE0 + unit);
        gl20.glBindTexture(glTarget, glHandle);
    }
    
    public void BindToDefault()
    {
        gl20.glBindTexture(glTarget, 0);
    }

    public int CreateGLHandle()
    {
      return gl20.glGenTexture();
    }

    public void SetGL20Info(int target, int handle)
    {
        glTarget = target;
        glHandle = handle;
    }
    
    public int GetGLTarget()
    {
        return glTarget;
    }
    
    public int GetGLHandle()
    {
        return glHandle;
    }
  
    public void AddManagedTexture()
    {
        Array<Texture> managedTextureArray = managedTextures.get(GameState.GetApp());
	if (managedTextureArray == null) 
            managedTextureArray = new Array<Texture>();
	managedTextureArray.add(this);
	managedTextures.put(GameState.GetApp(), managedTextureArray);
    }
    
    public void Dispose()
    {
        if (glHandle != 0)
        {
            GameState.nativeGraphics.glDeleteTexture(glHandle);
            glHandle = 0;
        }
    }
    
}
