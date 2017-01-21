function plugins_quorum_Libraries_Game_GameStateManager_() 
{
    /* The following are names of the effective static variables used here:
    plugins_quorum_Libraries_Game_GameStateManager_.application
    plugins_quorum_Libraries_Game_GameStateManager_.display
    plugins_quorum_Libraries_Game_GameStateManager_.graphics
    plugins_quorum_Libraries_Game_GameStateManager_.input
    plugins_quorum_Libraries_Game_GameStateManager_.nativePath
    plugins_quorum_Libraries_Game_GameStateManager_.operatingSystem    
    plugins_quorum_Libraries_Game_GameStateManager_.nativeGraphics
    */
    
    this.SetApplication$quorum_Libraries_Game_Application = function(app) 
    {
        plugins_quorum_Libraries_Game_GameStateManager_.application = app;
    };
    
    this.GetApplication = function() 
    {
        return plugins_quorum_Libraries_Game_GameStateManager_.application;
    };
    
    this.SetGameDisplay$quorum_Libraries_Game_GameDisplay = function(disp) 
    {
        plugins_quorum_Libraries_Game_GameStateManager_.display = disp;
    };
    
    this.GetGameDisplay = function() 
    {
        return plugins_quorum_Libraries_Game_GameStateManager_.display;
    };
    
    this.SetGameGraphics$quorum_Libraries_Game_Graphics_GraphicsManager = function(gl20) 
    {
        plugins_quorum_Libraries_Game_GameStateManager_.graphics = gl20;
        plugins_quorum_Libraries_Game_GameStateManager_.nativeGraphics = plugins_quorum_Libraries_Game_GameStateManager_.graphics.plugin_;
    };
    
    this.GetGameGraphics = function() 
    {
        return plugins_quorum_Libraries_Game_GameStateManager_.graphics;
    };
    
    this.GetInput = function() 
    {
        return plugins_quorum_Libraries_Game_GameStateManager_.input;
    };
    
    this.SetInput$quorum_Libraries_Game_GameInput = function(gameInput) 
    {
        plugins_quorum_Libraries_Game_GameStateManager_.input = gameInput;
    };
    
    this.SetNativePath$quorum_text = function(path) 
    {
        plugins_quorum_Libraries_Game_GameStateManager_.nativePath = path;
        //System.load(nativePath);
    };
    
    this.GetNativePath = function() 
    {
        return plugins_quorum_Libraries_Game_GameStateManager_.nativePath;
    };
    
    this.SetOperatingSystem$quorum_text = function(os) 
    {
        plugins_quorum_Libraries_Game_GameStateManager_.operatingSystem = os;
    };
    
    this.GetOperatingSystem = function() 
    {
        return plugins_quorum_Libraries_Game_GameStateManager_.operatingSystem;
    };
}
