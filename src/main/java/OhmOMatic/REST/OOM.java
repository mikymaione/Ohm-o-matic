/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.REST;

import OhmOMatic.ProtoBuffer.Common.standardRes;
import OhmOMatic.ProtoBuffer.HomeOuterClass.casa;
import OhmOMatic.ProtoBuffer.HomeOuterClass.listaCaseRes;
import OhmOMatic.ProtoBuffer.HomeOuterClass.parametriCasaReq;
import OhmOMatic.ProtoBuffer.StatOuterClass.parametriStatisticaReq;
import OhmOMatic.ProtoBuffer.StatOuterClass.parametriStatisticheReq;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.util.ArrayList;


@Path("OOM")
public final class OOM
{

    //region Casa
    @PUT
    @Path("/iscriviCasa")
    public standardRes iscriviCasa(parametriCasaReq par)
    {
        final var res = buildStandardRes(false, "excepsions!");

        return res;
    }

    @PUT
    @Path("/disiscriviCasa")
    public standardRes disiscriviCasa(parametriCasaReq par)
    {
        final var res = buildStandardRes(false, "excepsions!");

        return res;
    }


    @GET
    @Path("/elencoCase")
    public listaCaseRes elencoCase()
    {
        final var case_ = new ArrayList<casa>();

        final var res = listaCaseRes
                .newBuilder()
                .setStandardResponse(buildStandardRes(false, "excepsions!"))
                .addAllCase(case_)
                .build();

        return res;
    }
    //endregion


    //region Statistiche
    @PUT
    @Path("/aggiungiStatisticaLocale")
    public standardRes aggiungiStatisticaLocale(parametriStatisticaReq par)
    {
        final var res = buildStandardRes(false, "excepsions!");

        return res;
    }

    @PUT
    @Path("/aggiungiStatisticaGlobale")
    public standardRes aggiungiStatisticaGlobale(parametriStatisticaReq par)
    {
        final var res = buildStandardRes(false, "excepsions!");

        return res;
    }


    @GET
    @Path("/ultimeStatisticheCasa")
    public standardRes ultimeStatisticheCasa(parametriStatisticaReq par)
    {
        final var res = buildStandardRes(false, "excepsions!");

        return res;
    }

    @GET
    @Path("/ultimeStatisticheCondominio")
    public standardRes ultimeStatisticheCondominio(parametriStatisticheReq par)
    {
        final var res = buildStandardRes(false, "excepsions!");

        return res;
    }


    @GET
    @Path("/deviazioneStandardMediaCasa")
    public standardRes deviazioneStandardMediaCasa(parametriStatisticaReq par)
    {
        final var res = buildStandardRes(false, "excepsions!");

        return res;
    }

    @GET
    @Path("/deviazioneStandardMediaCondominio")
    public standardRes deviazioneStandardMediaCondominio(parametriStatisticheReq par)
    {
        final var res = buildStandardRes(false, "excepsions!");

        return res;
    }
    //endregion

    //region Common functions
    private standardRes buildStandardRes(boolean Ok, String errore)
    {
        return standardRes
                .newBuilder()
                .setOk(Ok)
                .setErrore(errore)
                .build();
    }
    //endregion
}