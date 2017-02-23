
function LoadSld( slideId )
{
	if( !g_supportsPPTHTML ) return
	if( slideId )
		parent.SldUpdated(slideId)
	g_origSz=parseInt(SlideObj.style.fontSize)
	g_origH=SlideObj.style.posHeight
	g_origW=SlideObj.style.posWidth
	g_scaleHyperlinks=(document.all.tags("AREA").length>0)
	if( g_scaleHyperlinks )
		InitHLinkArray()
	if( g_scaleInFrame||(IsWin("PPTSld") && parent.IsFullScrMode() ) )
		document.body.scroll="no"
	_RSW()
	if( IsWin("PPTSld") && parent.IsFullScrMode() )	{
		document.oncontextmenu=parent._CM;
		self.focus()
	}
}
function MakeSldVis( fTrans ) 
{
	fTrans=fTrans && g_showAnimation
	if( fTrans )
	{
		if( g_bgSound ) {
			idx=g_bgSound.indexOf(",");
			pptSound.src=g_bgSound.substr( 0, idx );
			pptSound.loop= -(parseInt(g_bgSound.substr(idx+1)));
		}
		SlideObj.filters.revealtrans.Apply()
	}
	SlideObj.style.visibility="visible"
	if( fTrans )
		SlideObj.filters.revealtrans.Play()
}
function MakeNotesVis() 
{
	if( !IsNts() ) return false 
	SlideObj.style.display="none"
	nObj = document.all.item("NotesObj")
	parent.SetHasNts(0)
	if( nObj ) { 
		nObj.style.display=""
		parent.SetHasNts(1)
	}
	return 1
}
function Redirect( frmId,sId )
{
	var str=document.location.hash,idx=str.indexOf('#')
	if(idx>=0) str=str.substr(1);
	if( window.name != frmId && ( sId != str) ) {
		obj = document.all.item("Main-File")
		window.location.href=obj.href+"#"+sId
		return 1
	}
	return 0
}
function HideMenu() { if( frames["PPTSld"] && PPTSld.document.all.item("ctxtmenu") && PPTSld.ctxtmenu.style.display!="none" ) { PPTSld.ctxtmenu.style.display='none'; return true } return false }
function IsWin( name ) { return window.name == name }
function IsNts() { return IsWin("PPTNts") }
function IsSldOrNts() { return( IsWin("PPTSld")||IsWin("PPTNts") ) }
function SupportsPPTAnimation() { return( navigator.platform == "Win32" && navigator.appVersion.indexOf("Windows")>0 ) }
function SupportsPPTHTML()
{
	var appVer=navigator.appVersion, msie=appVer.indexOf("MSIE "), ver=0
	if( msie >= 0 )
		ver=parseFloat( appVer.substring( msie+5, appVer.indexOf(";",msie) ) )
	else
		ver=parseInt(appVer)

	return( ver >= 4 && msie >= 0 )
}
var MHTMLPrefix = CalculateMHTMLPrefix(); 
function CalculateMHTMLPrefix()
{
	if ( document.location.protocol == 'mhtml:') { 
		href=new String(document.location.href) 
		Start=href.indexOf('!')+1 
		End=href.lastIndexOf('/')+1 
		if (End < Start) 
			return href.substring(0, Start) 
		else 
		return href.substring(0, End) 
	}
	return '';
}

function _RSW()
{
	if( !g_supportsPPTHTML || IsNts() ||
	  ( !g_scaleInFrame && (( window.name != "PPTSld" ) || !parent.IsFullScrMode()) ) )
		return

	cltWidth=document.body.clientWidth
	cltHeight=document.body.clientHeight
	factor=(1.0*cltWidth)/g_origW
	if( cltHeight < g_origH*factor )
		factor=(1.0*cltHeight)/g_origH

	newSize = g_origSz * factor
	if( newSize < 1 ) newSize=1

	s=SlideObj.style
	s.fontSize=newSize+"px"
	s.posWidth=g_origW*factor
	s.posHeight=g_origH*factor
	s.posLeft=(cltWidth-s.posWidth)/2
	s.posTop=(cltHeight-s.posHeight)/2

	if( g_scaleHyperlinks )
		ScaleHyperlinks( factor )
}
var AE_NONE = 0, AE_DIM = 1, AE_HIDE = 2, AE_HIDEIMDTLY = 3, LONG_INTERVAL = 10, SHORT_INTERVAL = 5, g_curAnim = null, g_vml = 0;
function ParseAnimations(){
	nr = document.all.tags("DIV").length;
	while(nr > 0) {
		rObj = document.all.tags("DIV").item(--nr);
		if(rObj.style.cssText != null) {
			build = rObj.style.cssText.indexOf("mso-build");
			if(build >= 0)
				AddAnimation(rObj);
		}
	}
	if(g_autoTrans){
		totT=0, autoAnims=0;
		rAnims=g_animManager.m_anims;
		nBuilds=g_animManager.m_numBuilds;
		for(x=nBuilds;x>0;x--)
		if(-1 != rAnims[x].m_secs){
			totT+=rAnims[x].m_secs;
			autoAnims++;
		}
		time=g_transSecs-totT;
		if(time<0) time=0;
		time=time/(nBuilds-autoAnims+1);
		for(x=nBuilds;x>0;x--)
			if(-1 == rAnims[x].m_secs)
				rAnims[x].m_secs=time;
	}
}
function AddAnimation(rObj){
	a =new Animation();
	a.m_obj =rObj;
	a.m_oldc =rObj.color;
	GetVal=rObj.style.getAttribute;
	var t;
	if( t=GetVal("mso-build") ) a.m_build=t-0;
	if( t=GetVal("mso-build-after-action") ) a.m_aAct=t-0;
	if( t=GetVal("mso-build-after-color") ) a.m_oldc=t;
	if( t=GetVal("mso-build-auto-secs") ) a.m_secs=t-0;
	if( t=GetVal("mso-build-avi") ) a.m_video=t;
	if( t=GetVal("mso-build-dual-id") ) a.m_dId=t;
	if( t=GetVal("mso-build-order") ) a.m_order=t-0;
	if( t=GetVal("mso-build-sound-name") ) a.m_sound=t;

	g_animManager.AddAnimation(a);
}
function ShowVGXObj(rObj){
	vmlId=null,ImgTag=null;
	curObj=rObj;
	sObj =document.all.item("SlideObj");
	while(null == ImgTag && null != curObj && sObj != curObj){
		ImgTag =curObj.children.tags("IMG").item(0);
		curObj =(curObj.parentElement.tagName == "DIV") ? curObj.parentElement : null
	}
	if(null !=ImgTag)
		vmlId =ImgTag.getAttribute("v:shapes");
	if(null !=vmlId) {
		idx =vmlId.indexOf(",");
		if(idx > 0) vmlId =vmlId.substr(0, idx);
		vgxObj =document.all.item(vmlId);
		if((null != vgxObj) && (null != vgxObj.parentElement) && (vgxObj.parentElement.style.display != "none")){
			vgxObj.parentElement.style.visibility = "visible";
			ImgTag.style.visibility = "hidden";
		}
		else
			rObj.style.visibility = "visible";
	}
}
function AnimationManager(){
	t=this;
	t.m_anims=new Array();
	t.m_numBuilds=0;
	t.m_activeObj=null;
	t.m_curBuild=1;
	t.m_timer=-1;
	t.m_objToHide=null;
	t.m_objToDim=null;
	t.m_colorToDim=-1;
	t.m_isPlaying=0;
	t.m_autoTimer=-1;
	t.Peek=AM_Peek;
	t.Next=AM_Next;
	t.Previous=AM_Previous;
	t.AddAnimation=AM_AddAnimation;
	t.ShowShapes=AM_ShowShapes;
	t.Swap=AM_Swap;
}
function AM_ShowShapes(){
	for(x=this.m_numBuilds; x>0; x--){
		if(g_vml)
			ShowVGXObj(this.m_anims[x].m_obj);
		aObj =this.m_anims[x].m_obj;
		if(aObj)
			aObj.style.visibility = "visible";
	}
	this.m_numBuilds =0;
}
function AM_Swap(cur){
	rList =this.m_anims;
	t =rList[cur+1];
	rList[cur+1] =rList[cur];
	rList[cur] =t;
}
function AM_AddAnimation(a){
	this.m_anims[++this.m_numBuilds] = a;
	this.m_curBuild =this.m_numBuilds;
	cur =this.m_numBuilds;
	if(a.m_order > 0){
		while(cur-- > 1 && Math.abs(this.m_anims[cur+1].m_order) > Math.abs(this.m_anims[cur].m_order))
			this.Swap(cur);
	}
	else {
		while(cur-- > 1 && Math.abs(this.m_anims[cur+1].m_order) >= Math.abs(this.m_anims[cur].m_order))
			this.Swap(cur);
	}
}
function AM_Peek(){
	if(g_animManager.m_curBuild > 0 && 0 < g_animManager.m_numBuilds) {
		secs =g_animManager.m_anims[g_animManager.m_curBuild].m_secs;
		if(secs != -1)
			g_animManager.m_autoTimer = window.setTimeout("_DocAction()", secs);
	}
	else if(1 == g_autoTrans && IsWin("PPTSld") && !parent.IsFramesMode()){
		setTimeout("parent.GoToNextSld()", g_transSecs);
	}
}
function AM_Next(){
	if(0 == this.m_numBuilds || (g_curAnim != null)) return 0; 
	success=0;
	if(this.m_autoTimer != -1)
		window.clearTimeout(this.m_autoTimer);
	if(this.m_objToHide != null)
		this.m_objToHide.style.visibility = "hidden";
	if(this.m_objToDim != null) {
		nr = document.all.length;
		while(nr-- > 0) {
			rObj =document.all.item(nr);
			if(this.m_objToDim.contains(rObj))
				rObj.style.color = this.m_colorToDim;
		}
	}
	if(this.m_curBuild > 0) {
		this.m_activeObj =this.m_anims[this.m_curBuild--];
		this.m_activeObj.Start();
		success =1;
	}
	return success;
}
function AM_Previous(){
	if((null == g_curAnim) && (this.m_curBuild <= this.m_numBuilds)) {
		this.m_curBuild++;
		var animObj = this.m_anims[this.m_curBuild];
		if(this.m_curBuild >= 1)
			animObj.m_obj.style.visibility = "hidden";
	}
}
function InitBuildEffects(){
	g_animManager =new AnimationManager();
	ParseAnimations();
	isFullScreen =(window.name == "PPTSld") && !parent.IsFramesMode();
	if(!g_showAnimation || (isFullScreen && parent.IsSldVisited()))
		g_animManager.ShowShapes();
	else if(!g_hasTrans)
		g_animManager.Peek();
	if(isFullScreen && !g_isKiosk)
		parent.SetSldVisited();
}
function Animation(){
	t=this;
	t.m_obj=0;
	t.m_build=0;
	t.m_dId="";
	t.m_aAct=0;
	t.m_secs=-1;
	t.m_order=0;
	t.m_sound="";
	t.m_oldc=0;
	t.m_video="";
	t.m_animObj=0; 
	t.m_timer=-1;
	t.m_steps=0;
	t.m_finalL=0;
	t.m_finalT=0;
	t.m_finalW=0;
	t.m_finalH=0;
	t.Start=A_Start;
	t.End=A_End;
	t.Animate=A_Animate;
	t.SetupMove=A_SetupMove;
	t.DoMove=A_DoMove;
	t.DoSpiral=A_DoSpiral;
	t.DoSwivel=A_DoSwivel;
	t.DoInplace=A_DoInplace;
}
function TickHandler(){
	if(null != g_curAnim) g_curAnim.Animate();
}
function GetAnimClass(dir){
	if(dir > 99) return 7; 
	else if(dir > 28) return 5; 
	else if(dir == 27) return 4; 
	else if(dir == 28) return 3; 
	else if((dir >= 12) && (dir < 16))return 2; 
	else return 1; 
}
function GetSteps(dir){
	animClass =GetAnimClass(dir);
	if(animClass == 5 || animClass == 7) return 1; 
	res=12;
	if(animClass >= 2 && animClass <= 4) res = 50;
	if(SlideObj.style.backgroundImage != "") res /= 2;
	return res;
}
function A_Start(){
	g_curAnim = this;
	this.SetupMove();
	if(this.m_sound)
		PlaySound(this.m_sound, false);
	animClass =GetAnimClass(this.m_build);
	if(5 == animClass)
		this.DoInplace();
	else {
		this.m_animObj.style.visibility = "visible";
		time = 0;
		if(7 == animClass) { // Appear and flash
			switch(this.m_build) {
				case 100: time = 300; break;
				case 101:
				case 200: time = 500; break;
				case 102: time = 1000; break;
			}
			if(this.m_build < 200)
				this.m_aAct = AE_HIDEIMDTLY;
		}
		else
			time =(animClass % 2 == 0) ? LONG_INTERVAL : SHORT_INTERVAL;
		this.m_timer =window.setInterval("TickHandler()", time);
	}
}
function A_End(){
	window.clearInterval(this.m_timer);
	if(this.m_video != "")
		PlayMedia(this.m_video);
	rStyle = this.m_obj.style;
	if(this.m_dId == ""){
		rStyle.pixelLeft =this.m_finalL;
		rStyle.pixelTop =this.m_finalT;
		rStyle.pixelWidth	=this.m_finalW;
		rStyle.pixelHeight =this.m_finalH;
	}
	else {
		if(this.m_aAct != AE_HIDEIMDTLY)
			rStyle.visibility = "visible";
		this.m_animObj.style.visibility = "hidden";
	}
	switch(this.m_aAct){
		case AE_NONE: 
			if(g_vml)
				ShowVGXObj(this.m_obj);
			break;
		case AE_DIM:
			if(g_vml)
				ShowVGXObj(this.m_obj);
			g_animManager.m_objToDim	=this.m_obj;
			g_animManager.m_colorToDim	=this.m_oldc;
			break;
		case AE_HIDE:
			g_animManager.m_objToHide	=this.m_obj;
			break;
		case AE_HIDEIMDTLY:
			rStyle.visibility ="hidden";
			break;
	}
	if(this.m_dId != "")
		this.m_animObj.style.visibility ="hidden";
	if(!g_animManager.m_isPlaying){
		g_curAnim = null;
		this.m_obj = null;
		g_animManager.Peek();
	}
}
function A_Animate(){
	switch(GetAnimClass(this.m_build)) {
		case 1:
		case 2: this.DoMove();	break;
		case 3: this.DoSpiral();	break;
		case 4: this.DoSwivel();	break;
	}
	if(--this.m_steps == 0) 
		this.End();
}
function A_SetupMove(){
	dir =this.m_build;
	this.m_steps =GetSteps(dir);
	rObj =this.m_dId != "" ? document.all.item(this.m_dId) : this.m_obj;
	this.m_animObj =rObj;
	offsetL=0;
	offsetT=0;
	parentObj=rObj.parentElement;
	while(parentObj.tagName != "BODY" && parentObj.id != "SlideObj"){
		offsetL	+=parentObj.offsetLeft;
		offsetT	+=parentObj.offsetTop;
		parentObj =parentObj.parentElement; 
	}
	rStyle = rObj.style;
	this.m_finalL =sLeft =rStyle.pixelLeft;
	this.m_finalT =sTop =rStyle.pixelTop;
	this.m_finalW =sWidth =rStyle.pixelWidth;
	this.m_finalH =sHeight =rStyle.pixelHeight;
	clientW=SlideObj.style.pixelWidth-offsetL;
	clientH=SlideObj.style.pixelHeight-offsetT;
 	if(dir < 100) {
		switch(dir) {
			case 0: case 12: sLeft=-(sWidth+offsetL); break;
			case 1: case 13: sTop=-(sHeight+offsetT);	break;
			case 2: case 14: sLeft=clientW; break;
			case 3: case 15: sTop=clientH; break;
			case 4: 
				sLeft=-(sWidth+offsetL); 
				sTop=-(sHeight+offsetT); 
				break;
			case 5: 
				sLeft=clientW; 
				sTop=-(sHeight+offsetT); 
				break;
			case 6: 
				sLeft=-(sWidth+offsetL); 
				sTop=clientH; 
				break;
			case 7: 
				sLeft=clientW; 
				sTop=clientH; 
				break;
			case 8: sLeft-=sWidth; break;
			case 9: sTop+=sHeight; break;
			case 10: sLeft+=sWidth; break;
			case 11: sTop-=sHeight; break;
			case 16:
				sLeft+=sWidth/2;
				sTop+=sHeight/2;
				sWidth=0;
				sHeight=0;
				break;
			case 17:
				sLeft+=sWidth/6;
				sTop+=sHeight/6;
				sWidth=sWidth*2/3;
				sHeight=sHeight*2/3;
				break;
			case 18:
				sLeft-=(3*sWidth)/2;
				sTop-=(3*sHeight)/2;
				sWidth=sWidth*4;
				sHeight=sHeight*4;
				break;
			case 19:
				sLeft-=sWidth/6;
				sTop-=sHeight/6;
				sWidth=sWidth*4/3;
				sHeight=sHeight*4/3;
				break;
			case 20:
				sLeft=clientW/2;
				sTop=clientH/2;
				sWidth=0;
				sHeight=0;
				break;
			case 21:
				sLeft= 0; 
				sTop=clientH; 
				sWidth=clientW; 
				sHeight=clientH;
				break;
			case 22:
				sLeft+=sWidth/2;
				sWidth=0;
				break;
			case 23: sWidth=0; break;
			case 24: sHeight=0; break;
			case 25:
				sLeft+=sWidth;
				sWidth=0;
				break;
			case 26:
				sTop+=sHeight;
				sHeight=0;
				break;
			case 27:
				sLeft+=sWidth/2;
				sWidth=0;
				break;
			case 28:
				sLeft=0;
				sTop=clientH;
				sWidth=0;
				sHeight=0;
				break;
		}
	}
	if(0 == sWidth) 
		sWidth++;
	rStyle.pixelLeft=sLeft;
	rStyle.pixelTop=sTop;
	rStyle.pixelWidth=sWidth;
	rStyle.pixelHeight=sHeight;
}
function A_DoInplace(){
	if(this.m_animObj.filters.revealtrans){
		this.m_animObj.filters.revealtrans.Apply();
		this.m_animObj.style.visibility ="visible";
		this.m_animObj.filters.revealtrans.Play();
	}
}
function A_DoMove(){
	rStyle = this.m_animObj.style;
	rStyle.pixelLeft +=(this.m_finalL - rStyle.pixelLeft)/this.m_steps;
	rStyle.pixelTop +=(this.m_finalT - rStyle.pixelTop)/this.m_steps;
	rStyle.pixelWidth +=(this.m_finalW - rStyle.pixelWidth)/this.m_steps;
	rStyle.pixelHeight +=(this.m_finalH - rStyle.pixelHeight)/this.m_steps;
} 
function A_DoSpiral(){
	ratio =this.m_steps / GetSteps(this.m_build);
	angle =2*ratio*3.14;
	cosa =Math.cos(angle);
	sina =Math.sin(angle);
	vx =(this.m_finalL + this.m_finalW / 2)*ratio;
	vy =(this.m_finalT + this.m_finalH / 2 - SlideObj.style.pixelHeight) * ratio;
	rStyle =this.m_animObj.style;
	rStyle.pixelLeft =this.m_finalL - vx * cosa - vy * sina;
	rStyle.pixelTop =this.m_finalT + vx * sina - vy * cosa;
	rStyle.pixelWidth =this.m_finalW * (1 - ratio);
	rStyle.pixelHeight =this.m_finalH * (1 - ratio);
}
function A_DoSwivel(){
	rStyle =this.m_animObj.style;
	angle =this.m_steps / GetSteps(this.m_build) * 5 * 3.14 / 2;
	difX =Math.abs(this.m_finalW * Math.cos(angle));
	rStyle.pixelLeft =this.m_finalL + (this.m_finalW - difX) / 2;
	if(0 == difX) difX++;
	rStyle.pixelWidth =difX;
}
function DocumentOnClick()
{
	if( IsNts() || parent.HideMenu() ) return
	_DocAction()
}
function _DocAction()
{
	if( g_animManager && g_animManager.m_isPlaying && g_curAnim )
		StopMedia( g_curAnim.m_video );

	if( ( g_showAnimation && !g_curAnim && 
		g_animManager && g_animManager.Next() ) )
		return;

	if( ( g_allowAdvOnClick && (window.name=="PPTSld") && !parent.IsFramesMode() && 
		!g_curAnim ) || (!g_curAnim && event && event.keyCode==32) )
		parent.GoToNextSld();
}


var g_supportsPPTHTML = SupportsPPTHTML(), g_scaleInFrame = true, gId="", g_bgSound="",
    g_scaleHyperlinks = false, g_allowAdvOnClick = false, g_showInBrowser = false;
var g_showAnimation = g_supportsPPTHTML && SupportsPPTAnimation() && g_showInBrowser;
var g_hasTrans = false, g_autoTrans = false, g_transSecs = 0;
var g_isKiosk = 0;var g_animManager = null;
